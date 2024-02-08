package isv.sap.payment.addon.strategy.impl;

import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;


public class WeChatSaleRequester extends AbstractSaleRequester
{
    private static final int DEFAULT_TRANSACTION_TIMEOUT = 360;

    @Override
    public PaymentServiceResult internalInitiateSale(
            final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType,
            final String merchantId,
            final Map<String, Object> optionalParams)
    {
        final SaleRequestBuilder saleBuilder = new SaleRequestBuilder()
                .setMerchantId(merchantId)
                .setApPaymentType(paymentType)
                .setSuccessURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL), true) + paymentType.name())
                .setCancelURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL), true))
                .setMerchantDescriptor(getProperty(MERCHANT_NAME))
                .setTransactionTimeout(getIntProperty(
                        IsvPaymentAddonConstants.AlternativePayments.WECHAT_SALE_TRANSACTION_TIMEOUT, DEFAULT_TRANSACTION_TIMEOUT))
                .addParam(ORDER, cart);

        return getPaymentServiceExecutor().execute(saleBuilder.build());
    }

    @Override
    public boolean internalSupports(final AlternativePaymentMethod paymentType)
    {
        return paymentType.equals(AlternativePaymentMethod.WQR);
    }
}
