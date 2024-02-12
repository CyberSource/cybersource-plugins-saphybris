package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import java.util.Map;

import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.COMMERCE_INDICATOR_MAPPER;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.ECI;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.INTERNET_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.PAYMENT_CRYPTOGRAM;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CARD_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_CAVV;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DECRYPTION_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_DATA;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_TOKEN;
import static isv.cjl.payment.enums.DecryptionType.MERCHANT;
import static java.util.Optional.ofNullable;

public class VisaMerchDecryptionStrategy extends AbstractAuthorizationRequestConverterStrategy
{
    public boolean supports(final PaymentServiceRequest request)
    {
        return MERCHANT.equals(request.getRequiredParam(DECRYPTION_TYPE))
                && CardType.VISA.equals(request.getRequiredParam(CARD_TYPE));
    }

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Map<String, Object> paymentToken = source.getRequiredParam(PAYMENT_TOKEN);
        final Map paymentData = (Map) paymentToken.get(PAYMENT_DATA);
        final String paymentCryptogram = (String) paymentData.get(PAYMENT_CRYPTOGRAM);
        final String commerceIndicator = ofNullable(paymentData.get(ECI))
                .map(COMMERCE_INDICATOR_MAPPER::get)
                .orElse(INTERNET_COMMERCE_INDICATOR);

        return createMerchDecryptBaseRequest(source)
                .addParam(CC_AUTH_SERVICE_CAVV, paymentCryptogram)
                .addParam(CC_AUTH_SERVICE_COMMERCE_INDICATOR, commerceIndicator)
                .request();
    }
}
