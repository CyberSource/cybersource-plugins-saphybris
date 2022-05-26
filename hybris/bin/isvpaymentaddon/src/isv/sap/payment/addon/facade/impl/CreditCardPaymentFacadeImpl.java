package isv.sap.payment.addon.facade.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorfacades.payment.data.PaymentSubscriptionResultData;
import de.hybris.platform.acceleratorfacades.payment.impl.DefaultPaymentFacade;
import de.hybris.platform.acceleratorservices.payment.data.CreateSubscriptionRequest;
import de.hybris.platform.acceleratorservices.payment.data.CreateSubscriptionResult;
import de.hybris.platform.acceleratorservices.payment.data.CustomerInfoData;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.acceleratorservices.payment.data.PaymentErrorField;
import de.hybris.platform.acceleratorservices.payment.data.PaymentSubscriptionResultItem;
import de.hybris.platform.acceleratorservices.payment.strategies.ClientReferenceLookupStrategy;
import de.hybris.platform.acceleratorservices.payment.strategies.CreateSubscriptionRequestStrategy;
import de.hybris.platform.acceleratorservices.payment.strategies.CreateSubscriptionResultValidationStrategy;
import de.hybris.platform.acceleratorservices.payment.strategies.PaymentResponseInterpretationStrategy;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import io.jsonwebtoken.Claims;

import isv.cjl.payment.data.enrollment.OrderData;
import isv.cjl.payment.enums.TransactionMode;
import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.jwt.JwtService;
import isv.sap.payment.addon.facade.CreditCardPaymentFacade;
import isv.sap.payment.addon.facade.PaymentInfoFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REVIEW;
import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.FLEX_TOKEN;
import static isv.sap.payment.constants.IsvPaymentConstants.ReasonCode.ENROLLED_CODE;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * A custom implementation of payment facade that, on top of {@link DefaultPaymentFacade}
 * adds payment logic logic for PayPal and Alternative payments.
 */
public class CreditCardPaymentFacadeImpl extends AbstractPaymentFacade implements CreditCardPaymentFacade
{
    @Resource
    private CreateSubscriptionRequestStrategy createSubscriptionRequestStrategy;

    @Resource
    private Converter<CreateSubscriptionRequest, PaymentData> paymentDataConverter;

    @Resource
    private PaymentResponseInterpretationStrategy paymentResponseInterpretationStrategy;

    @Resource
    private ClientReferenceLookupStrategy clientReferenceLookupStrategy;

    @Resource
    private CreateSubscriptionResultValidationStrategy createSubscriptionResultValidationStrategy;

    @Resource
    private ModelService modelService;

    @Resource
    private CartService cartService;

    @Resource(name = "extPaymentInfoFacade")
    private PaymentInfoFacade paymentInfoFacade;

    @Resource(name = "isv.sap.payment.jwtService")
    private JwtService jwtService;

    @Resource
    private CustomerNameStrategy customerNameStrategy;

    @Resource
    private Converter<AbstractOrderModel, isv.cjl.payment.data.enrollment.OrderData> enrollmentPayloadConverter;

    @Resource
    private Converter<CustomerInfoData, AddressModel> creditCardReverseAddressConverter;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public PaymentData beginCreatePayment(final String responseUrl)
    {
        final String fullResponseUrl = getFullResponseUrl(responseUrl, true);
        final String siteName = getCurrentSiteName();

        final CustomerModel customerModel = getCurrentUserForCheckout();
        final AddressModel paymentAddress = getDefaultPaymentAddress(customerModel);

        final CreateSubscriptionRequest request = createSubscriptionRequestStrategy.createSubscriptionRequest(
                siteName, fullResponseUrl, fullResponseUrl, null, customerModel, null, paymentAddress);

        final PaymentData data = paymentDataConverter.convert(request);

        return Optional.ofNullable(data).orElse(new PaymentData());
    }

    @Override
    public PaymentSubscriptionResultData completeCreatePayment(final Map<String, String> parameters,
            final boolean saveInAccount)
    {
        final CustomerModel customerModel = getCurrentUserForCheckout();
        final PaymentSubscriptionResultItem paymentSubscription = completeCreatePayment(customerModel,
                saveInAccount, parameters);

        final CartModel cart = cartService.getSessionCart();
        cart.setPaymentInfo(paymentSubscription.getStoredPayment());
        modelService.save(cart);

        return getPaymentSubscriptionResultDataConverter().convert(paymentSubscription);
    }

    private PaymentSubscriptionResultItem completeCreatePayment(final CustomerModel customerModel,
            final boolean saveInAccount, final Map<String, String> parameters)
    {
        final PaymentSubscriptionResultItem paymentSubscription = new PaymentSubscriptionResultItem();
        final Map<String, PaymentErrorField> errors = new HashMap<>();
        paymentSubscription.setErrors(errors);

        final CreateSubscriptionResult response = paymentResponseInterpretationStrategy.interpretResponse(parameters,
                clientReferenceLookupStrategy.lookupClientReferenceId(), errors);

        validateParameterNotNull(response, "Response is required");

        if (!createSubscriptionResultValidationStrategy.validateCreateSubscriptionResult(errors, response).isEmpty())
        {
            return paymentSubscription;
        }

        paymentSubscription.setSuccess(Boolean.TRUE);

        final PaymentInfoModel paymentInfoModel = savePaymentSubscription(customerModel, response.getCustomerInfoData(),
                saveInAccount);

        paymentSubscription.setStoredPayment(paymentInfoModel);
        modelService.save(paymentInfoModel);

        return paymentSubscription;
    }

    private PaymentInfoModel savePaymentSubscription(final CustomerModel customerModel,
            final CustomerInfoData customerInfoData,
            final boolean saveInAccount)
    {
        validateParameterNotNull(customerInfoData, "customerInfoData cannot be null");

        final AddressModel billingAddress = modelService.create(AddressModel.class);
        creditCardReverseAddressConverter.convert(customerInfoData, billingAddress);

        final PaymentInfoModel paymentInfoModel = paymentInfoFacade
                .createPaymentInfo(billingAddress, customerModel, saveInAccount);

        billingAddress.setOwner(paymentInfoModel);

        if (CustomerType.GUEST.equals(customerModel.getType()))
        {
            final String name = customerNameStrategy
                    .getName(customerInfoData.getBillToFirstName(), customerInfoData.getBillToLastName());
            customerModel.setName(name);
            modelService.save(customerModel);
        }

        modelService.saveAll(paymentInfoModel, billingAddress);
        modelService.refresh(customerModel);

        final List<PaymentInfoModel> paymentInfoModels = new ArrayList<>(
                customerModel.getPaymentInfos());
        if (!paymentInfoModels.contains(paymentInfoModel))
        {
            paymentInfoModels.add(paymentInfoModel);
            if (saveInAccount)
            {
                customerModel.setPaymentInfos(paymentInfoModels);
                modelService.save(customerModel);
            }

            modelService.save(paymentInfoModel);
            modelService.refresh(customerModel);
        }
        return paymentInfoModel;
    }

    @Override
    public boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken)
    {
        final IsvPaymentTransactionEntryModel authorizationEntry = doFlexCreditCardAuthorization(cart, flexToken);

        return isTransactionInState(authorizationEntry, ACCEPT, REVIEW);
    }

    private IsvPaymentTransactionEntryModel doFlexCreditCardAuthorization(final CartModel cart, final String flexToken)
    {
        final PaymentServiceResult authorizationResult = executeRequest(
                new isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder()
                        .setMerchantId(getMerchantService().getCurrentMerchant(CREDIT_CARD).getId())
                        .addParam(ORDER, cart)
                        .addParam(FLEX_TOKEN, flexToken)
                        .setAuthValidateServiceRun(false)
                        .build()
        );

        return authorizationResult.getData(TRANSACTION);
    }

    @Override
    public boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken,
            final IsvPaymentTransactionEntryModel enrollmentTransaction)
    {
        checkEnrollReply(enrollmentTransaction, enrollmentTransaction.getProperties());

        //Authorize transaction was bundled with enrollment, no need to perform it again
        return paymentTransactionService
                .createAuthorizationTxEntryFromEnrollment(enrollmentTransaction)
                .filter(IsvPaymentTransactionEntryModel.class::isInstance)
                .map(IsvPaymentTransactionEntryModel.class::cast)
                .map(txEntry -> isTransactionInState(txEntry, ACCEPT, REVIEW)).orElse(false);
    }

    @Override
    public boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken,
            final String authJwt)
    {
        final Claims decodedJwt = jwtService
                .decodeJwt(authJwt, getSiteConfigService().getProperty("isv.payment.customer.3ds.jwt.api.key"));

        final String transactionId = ofNullable(decodedJwt.get("Payload"))
                .map(payload -> ((Map) payload).get("Payment"))
                .map(payload -> (String) ((Map) payload).get("ProcessorTransactionId")).orElse(null);

        final Integer errorNumber = ofNullable(decodedJwt.get("Payload"))
                .map(payload -> (Integer) ((Map) payload).get("ErrorNumber"))
                .orElse(null);

        if (isNotBlank(transactionId) && Objects.equals(0, errorNumber))
        {
            final IsvPaymentTransactionEntryModel authorizationEntry = doFlexCreditCardAuthorizationWithValidation(
                    cart, flexToken, transactionId);

            return isTransactionInState(authorizationEntry, ACCEPT, REVIEW);
        }

        return false;
    }


    private void checkEnrollReply(final IsvPaymentTransactionEntryModel enrollmentTransactionEntry,
            final Map<String, String> properties)
    {
        if (ENROLLED_CODE.equals(properties.get("payerAuthEnrollReplyReasonCode")))
        {
            throw new PaymentException(
                    String.format("Transaction [%s] requires validation.", enrollmentTransactionEntry.getCode()));
        }
    }

    private IsvPaymentTransactionEntryModel doFlexCreditCardAuthorizationWithValidation(final CartModel cart,
            final String flexToken, final String authTransactionId)
    {
        final PaymentServiceResult authorizationResult = executeRequest(
                new isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder()
                        .setMerchantId(getMerchantID(CREDIT_CARD))
                        .setAuthValidateTransactionId(authTransactionId)
                        .setAuthValidateServiceRun(true)
                        .addParam(ORDER, cart)
                        .addParam(FLEX_TOKEN, flexToken)
                        .build()
        );

        return authorizationResult.getData(TRANSACTION);
    }

    @Override
    public String createEnrollmentJwt()
    {
        final OrderData payload = enrollmentPayloadConverter
                .convert(cartService.getSessionCart());

        return jwtService
                .createEnrollmentJwt(getSiteConfigService().getProperty("isv.payment.customer.3ds.jwt.api.key"),
                        payload);
    }

    @Override
    public IsvPaymentTransactionEntryModel enrollCreditCard(final String referenceId, final String transientToken)
    {
        final PaymentServiceResult enrollmentResult = executeRequest(
                new isv.cjl.payment.service.executor.request.builder.creditcard.EnrollmentRequestBuilder()
                        .setReferenceId(referenceId)
                        .setTransactionMode(TransactionMode.ECOMMERCE)
                        .setMerchantId(getMerchantID(CREDIT_CARD))
                        .addParam(ORDER, cartService.getSessionCart())
                        .addParam(FLEX_TOKEN, transientToken)
                        .build());
        return enrollmentResult.getData(TRANSACTION);
    }

    @Override
    public boolean is3dsEnabled()
    {
        return getMerchantService().is3dsEnabled();
    }
}
