package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import java.util.Map;

import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.MASTERCARD_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.APPLE_PAY_UCAF_COLLECTION_INDICATOR_VALUE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CARD_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DECRYPTION_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.ONLINE_PAYMENT_CRYPTOGRAM;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_DATA;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.UCAF_AUTHENTICATION_DATA;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.UCAF_COLLECTION_INDICATOR;
import static isv.cjl.payment.enums.DecryptionType.MERCHANT;

public class MasterCardMerchDecryptionStrategy extends AbstractAuthorizationRequestConverterStrategy
{
    public boolean supports(final PaymentServiceRequest request)
    {
        return MERCHANT.equals(request.getRequiredParam(DECRYPTION_TYPE))
                && CardType.MASTERCARD_EUROCARD.equals(request.getRequiredParam(CARD_TYPE));
    }

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Map<String, Object> paymentToken = source.getRequiredParam(PAYMENT_TOKEN);
        final Map paymentData = (Map) paymentToken.get(PAYMENT_DATA);
        final String paymentCryptogram = (String) paymentData.get(ONLINE_PAYMENT_CRYPTOGRAM);

        return createMerchDecryptBaseRequest(source)
                .addParam(UCAF_AUTHENTICATION_DATA, paymentCryptogram)
                .addParam(UCAF_COLLECTION_INDICATOR, APPLE_PAY_UCAF_COLLECTION_INDICATOR_VALUE)
                .addParam(CC_AUTH_SERVICE_COMMERCE_INDICATOR, MASTERCARD_COMMERCE_INDICATOR)
                .request();
    }
}
