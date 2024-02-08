package isv.sap.payment.addon.strategy.impl;

import java.util.Map;
import java.util.stream.Stream;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.alternative.InitiateRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;

public class AlipaySaleRequester extends AbstractSaleRequester
{
    public static final String RECONCILATION_ID = "isv.payment.alternativepayment.alipay.reconcilationId";

    @Override
    public PaymentServiceResult internalInitiateSale(
            final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType,
            final String merchantId,
            final Map<String, Object> optionalParams)
    {
        final InitiateRequestBuilder initiateRequestBuilder = new InitiateRequestBuilder()
                .setMerchantId(merchantId)
                .setApPaymentType(paymentType)
                .setProductName(getConfigurationService().getConfiguration().getString(MERCHANT_NAME))
                .setReturnUrl(convertToAbsoluteURL(getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL), true) + paymentType.name())
                .addParam(ORDER, cart);

        final String reconciliationID = getConfigurationService().getConfiguration().getString(RECONCILATION_ID);
        if (StringUtils.isNotBlank(reconciliationID))
        {
            return getPaymentServiceExecutor()
                    .execute(initiateRequestBuilder.setReconciliationID(reconciliationID).build());
        }
        return getPaymentServiceExecutor().execute(initiateRequestBuilder.build());
    }

    @Override
    public boolean internalSupports(final AlternativePaymentMethod paymentType)
    {
        return Stream.of(AlternativePaymentMethod.APY, AlternativePaymentMethod.AYM)
                .anyMatch(type -> type.equals(paymentType));
    }
}
