package isv.sap.payment.addon.strategy.impl;

import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;

public class SofortSaleRequester extends AbstractSaleRequester
{
    @Override
    public PaymentServiceResult internalInitiateSale(final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType,
            final String merchantId, final Map<String, Object> optionalParams)
    {
        return getPaymentServiceExecutor().execute(createSaleBuilder(merchantId, cart, paymentType).build());
    }

    protected SaleRequestBuilder createSaleBuilder(final String merchantId, final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType)
    {
        return new SaleRequestBuilder()
                .setMerchantId(merchantId)
                .setApPaymentType(paymentType)
                .setSuccessURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL), true) + paymentType.name())
                .setCancelURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL), true))
                .setFailureURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL), true))
                .setMerchantDescriptor(getProperty(MERCHANT_NAME))
                .addParam(ORDER, cart);
    }

    @Override
    public boolean internalSupports(final AlternativePaymentMethod paymentType)
    {
        return AlternativePaymentMethod.SOF.equals(paymentType);
    }
}
