package isv.sap.payment.addon.strategy.impl;

import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.alternative.AuthorizationRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.sap.payment.enums.AlternativePaymentMethod.KLI;

public class KlarnaAuthRequester extends AbstractSaleRequester
{
    @Override
    protected PaymentServiceResult internalInitiateSale(final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType, final String merchantId,
            final Map<String, Object> optionalParams)
    {
        final Object preAuthToken = optionalParams.get(
                IsvPaymentAddonConstants.AlternativePayments.KLARNA_AUTH_TOKEN);

        if (preAuthToken == null)
        {
            throw new IllegalArgumentException("For Klarna preAuthToken has to be present during auth");
        }

        final PaymentServiceRequest request = new AuthorizationRequestBuilder()
                .setMerchantId(merchantId)
                .addParam(ORDER, cart)
                .setPreapprovalToken((String) preAuthToken)
                .setCancelURL(convertToAbsoluteURL(getSiteConfigService().getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL), true))
                .setFailureURL(convertToAbsoluteURL(getSiteConfigService().getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL), true))
                .setSuccessURL(convertToAbsoluteURL(getSiteConfigService().getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL), true) + KLI
                        .getCode())
                .setBillToLanguage(getSiteConfigService().getProperty(
                        IsvPaymentAddonConstants.AlternativePayments.KLARNA_LANGUAGE))
                .build();

        return getPaymentServiceExecutor().execute(request);
    }

    @Override
    protected boolean internalSupports(final AlternativePaymentMethod paymentType)
    {
        return paymentType.equals(AlternativePaymentMethod.KLI);
    }
}
