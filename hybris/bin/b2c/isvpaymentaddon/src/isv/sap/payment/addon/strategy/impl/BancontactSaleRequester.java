package isv.sap.payment.addon.strategy.impl;

import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;

public class BancontactSaleRequester extends SofortSaleRequester
{
    @Override
    public PaymentServiceResult internalInitiateSale(final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType, final String merchantId,
            final Map<String, Object> optionalParams)
    {
        final SaleRequestBuilder saleBuilder = createSaleBuilder(merchantId, cart, paymentType)
                .setFailureURL(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL), true));

        return getPaymentServiceExecutor().execute(saleBuilder.build());
    }

    @Override
    public boolean internalSupports(final AlternativePaymentMethod paymentType)
    {
        return paymentType.equals(AlternativePaymentMethod.MCH);
    }
}
