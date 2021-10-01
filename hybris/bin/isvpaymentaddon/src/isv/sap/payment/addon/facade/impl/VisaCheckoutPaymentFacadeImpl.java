package isv.sap.payment.addon.facade.impl;

import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;
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
import static org.apache.commons.lang.StringUtils.isNotBlank;

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
    private CommonI18NService commonI18NService;

    @Override
    public boolean authorizeVisaCheckoutPayment(final CartModel sessionCart, final String callId)
    {
        final IsvPaymentTransactionEntryModel vcAuthorizationEntry = performVCAuthorization(sessionCart, callId);
        if (isTransactionInState(vcAuthorizationEntry, ACCEPT, REVIEW))
        {
            paymentTransactionService.addProperty(VCRequestFields.VC_ORDER_ID, callId, vcAuthorizationEntry);

            return true;
        }

        LOG.error("Visa Checkout authorization operation wasn't successful, returned status: " + vcAuthorizationEntry
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

        LOG.error("Visa Checkout Get Data operation wasn't successful, returned status: " + vcGetEntry
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

        return (IsvPaymentTransactionEntryModel) vcAuthorizationResult.getData(TRANSACTION);
    }

    private IsvPaymentTransactionEntryModel performGetVCData(final CartModel sessionCart, final String callId)
    {
        final PaymentServiceResult getVCDataResult = executeRequest(
                new GetRequestBuilder()
                        .setVcOrderId(callId)
                        .setMerchantId(getMerchantID(isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT))
                        .addParam(ORDER, sessionCart)
                        .build());

        return (IsvPaymentTransactionEntryModel) getVCDataResult.getData(TRANSACTION);
    }

    private void updateCartAddressesWithVCGetData(final IsvPaymentTransactionEntryModel vcGetEntry,
            final CartModel sessionCart)
    {
        final Map<String, String> paymentTransactionProperties = vcGetEntry.getProperties();
        final PaymentInfoModel paymentInfo = paymentInfoFacade.resolvePaymentInfo(sessionCart, getCurrentUserForCheckout());

        final AddressModel deliveryAddress = sessionCart.getDeliveryAddress();
        deliveryAddress.setTown(paymentTransactionProperties.get(VCRequestFields.SHIP_TO_CITY));
        setCountryToAddress(deliveryAddress, paymentTransactionProperties.get(VCRequestFields.SHIP_TO_COUNTRY));
        setRegionToAddress(deliveryAddress, paymentTransactionProperties.get(VCRequestFields.SHIP_TO_STATE));
        setFirstLastNameToAddress(deliveryAddress, paymentTransactionProperties.get(VCRequestFields.SHIP_TO_NAME));
        deliveryAddress.setPhone1(paymentTransactionProperties.get(VCRequestFields.SHIP_TO_PHONE_NUMBER));
        deliveryAddress.setPostalcode(paymentTransactionProperties.get(VCRequestFields.SHIP_TO_POSTAL_CODE));
        deliveryAddress.setLine1(paymentTransactionProperties.get(VCRequestFields.SHIP_TO_STREET1));
        deliveryAddress.setLine2(paymentTransactionProperties.get(VCRequestFields.SHIP_TO_STREET2));

        final AddressModel billingAddress = paymentInfo.getBillingAddress();

        billingAddress.setTown(paymentTransactionProperties.get(VCRequestFields.BILL_TO_CITY));
        setCountryToAddress(billingAddress, paymentTransactionProperties.get(VCRequestFields.BILL_TO_COUNTRY));
        setRegionToAddress(billingAddress, paymentTransactionProperties.get(VCRequestFields.BILL_TO_STATE));
        setFirstLastNameToAddress(billingAddress, paymentTransactionProperties.get(VCRequestFields.BILL_TO_NAME));
        billingAddress.setPhone1(paymentTransactionProperties.get(VCRequestFields.BILL_TO_PHONE_NUMBER));
        billingAddress.setPostalcode(paymentTransactionProperties.get(VCRequestFields.BILL_TO_POSTAL_CODE));
        billingAddress.setLine1(paymentTransactionProperties.get(VCRequestFields.BILL_TO_STREET1));
        billingAddress.setLine2(paymentTransactionProperties.get(VCRequestFields.BILL_TO_STREET2));

        modelService.saveAll(deliveryAddress, billingAddress);

        setDeliveryModeIfNecessary(sessionCart);
    }

    private void setFirstLastNameToAddress(final AddressModel address, final String fullName)
    {
        if (isNotBlank(fullName))
        {
            final String[] splitFullName = fullName.split(" ", 2);
            address.setFirstname(splitFullName[0]);
            address.setLastname(splitFullName[1]);
        }
    }

    protected void setDeliveryModeIfNecessary(final CartModel sessionCart)
    {
        if (isNull(sessionCart.getDeliveryMode()))
        {
            final String deliveryMode = checkoutFacade.getSupportedDeliveryModes().stream().findFirst().get().getCode();

            checkoutFacade.setDeliveryMode(deliveryMode);
        }
    }

    private void setCountryToAddress(final AddressModel address, final String billToCountry)
    {
        final CountryModel country = commonI18NService.getCountry(billToCountry);
        address.setCountry(country);
    }

    private void setRegionToAddress(final AddressModel address, final String state)
    {
        if (StringUtils.isNotEmpty(state))
        {
            address.setRegion(
                    commonI18NService.getRegion(address.getCountry(), address.getCountry().getIsocode() + "-" + state));
        }
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }

    public void setCheckoutFacade(final AcceleratorCheckoutFacade checkoutFacade)
    {
        this.checkoutFacade = checkoutFacade;
    }

    public void setPaymentInfoFacade(final PaymentInfoFacade paymentInfoFacade)
    {
        this.paymentInfoFacade = paymentInfoFacade;
    }

    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }

    public void setCommonI18NService(final CommonI18NService commonI18NService)
    {
        this.commonI18NService = commonI18NService;
    }
}
