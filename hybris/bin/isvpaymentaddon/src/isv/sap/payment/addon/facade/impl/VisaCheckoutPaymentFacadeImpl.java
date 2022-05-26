package isv.sap.payment.addon.facade.impl;

import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.visacheckout.AuthorizationRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.visacheckout.GetRequestBuilder;
import isv.sap.payment.addon.facade.PaymentInfoFacade;
import isv.sap.payment.addon.facade.VisaCheckoutPaymentFacade;
import isv.sap.payment.constants.IsvPaymentConstants.VCRequestFields;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REVIEW;
import static java.util.Objects.isNull;

public class VisaCheckoutPaymentFacadeImpl extends AbstractPaymentFacade implements VisaCheckoutPaymentFacade
{
    private static final Logger LOG = LoggerFactory.getLogger(VisaCheckoutPaymentFacadeImpl.class);

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "acceleratorCheckoutFacade")
    private AcceleratorCheckoutFacade checkoutFacade;

    @Resource(name = "extPaymentInfoFacade")
    private PaymentInfoFacade paymentInfoFacade;

    @Resource
    private ModelService modelService;

    @Resource
    private Converter<Map<String, String>, AddressModel> visaCheckoutBillingAddressReverseConverter;

    @Resource
    private Converter<Map<String, String>, AddressModel> visaCheckoutDeliveryAddressReverseConverter;

    @Override
    public boolean authorizeVisaCheckoutPayment(final CartModel sessionCart, final String callId)
    {
        final IsvPaymentTransactionEntryModel vcAuthorizationEntry = performVCAuthorization(sessionCart, callId);
        if (isTransactionInState(vcAuthorizationEntry, ACCEPT, REVIEW))
        {
            paymentTransactionService.addProperty(VCRequestFields.VC_ORDER_ID, callId, vcAuthorizationEntry);

            return true;
        }

        LOG.error("Visa Checkout authorization operation wasn't successful, returned status: {}", vcAuthorizationEntry
                .getTransactionStatus());

        return false;
    }

    @Override
    public boolean authorizeVisaCheckoutPayment(final CartModel sessionCart, final String callId,
            final boolean performGetDataFirst)
    {
        boolean shouldAuthorize = true;

        if (performGetDataFirst)
        {
            shouldAuthorize = updateCartAddressesWithVCGetData(sessionCart, callId);
        }

        return shouldAuthorize && authorizeVisaCheckoutPayment(sessionCart, callId);
    }

    @Override
    public boolean updateCartAddressesWithVCGetData(final CartModel sessionCart, final String callId)
    {
        final IsvPaymentTransactionEntryModel vcGetEntry = performGetVCData(sessionCart, callId);
        if (isTransactionInState(vcGetEntry, ACCEPT))
        {
            updateCartAddressesWithVCGetData(vcGetEntry, sessionCart);

            return true;
        }

        LOG.error("Visa Checkout Get Data operation wasn't successful, returned status: {}", vcGetEntry
                .getTransactionStatus());

        return false;
    }

    private IsvPaymentTransactionEntryModel performVCAuthorization(final CartModel sessionCart, final String callId)
    {
        final PaymentServiceResult vcAuthorizationResult = executeRequest(
                new AuthorizationRequestBuilder()
                        .setVcOrderId(callId)
                        .setMerchantId(getMerchantID(isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT))
                        .addParam(ORDER, sessionCart)
                        .build());

        return vcAuthorizationResult.getData(TRANSACTION);
    }

    private IsvPaymentTransactionEntryModel performGetVCData(final CartModel sessionCart, final String callId)
    {
        final PaymentServiceResult getVCDataResult = executeRequest(
                new GetRequestBuilder()
                        .setVcOrderId(callId)
                        .setMerchantId(getMerchantID(isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT))
                        .addParam(ORDER, sessionCart)
                        .build());

        return getVCDataResult.getData(TRANSACTION);
    }

    private void updateCartAddressesWithVCGetData(final IsvPaymentTransactionEntryModel vcGetEntry,
            final CartModel sessionCart)
    {
        final Map<String, String> paymentTransactionProperties = vcGetEntry.getProperties();
        final PaymentInfoModel paymentInfo = paymentInfoFacade.resolvePaymentInfo(sessionCart, getCurrentUserForCheckout());

        final AddressModel deliveryAddress = sessionCart.getDeliveryAddress();
        visaCheckoutDeliveryAddressReverseConverter.convert(paymentTransactionProperties, deliveryAddress);

        final AddressModel billingAddress = paymentInfo.getBillingAddress();
        visaCheckoutBillingAddressReverseConverter.convert(paymentTransactionProperties, billingAddress);

        modelService.saveAll(deliveryAddress, billingAddress);

        setDeliveryModeIfNecessary(sessionCart);
    }

    protected void setDeliveryModeIfNecessary(final CartModel sessionCart)
    {
        if (isNull(sessionCart.getDeliveryMode()))
        {
            checkoutFacade.getSupportedDeliveryModes().stream()
                    .findFirst()
                    .map(DeliveryModeData::getCode)
                    .ifPresent(deliveryMode -> checkoutFacade.setDeliveryMode(deliveryMode));
        }
    }
}
