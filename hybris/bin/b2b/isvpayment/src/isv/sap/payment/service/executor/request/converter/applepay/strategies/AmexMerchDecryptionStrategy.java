package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import java.util.Arrays;
import java.util.Map;

import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.AMEX_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.CARD_TYPE;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.DECRYPTION_TYPE;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.PAYMENT_CRYPTOGRAM;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_DATA;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_CAVV;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_COMMERCE_INDICATOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_SERVICE_XID;
import static isv.cjl.payment.enums.DecryptionType.MERCHANT;

public class AmexMerchDecryptionStrategy extends AbstractAuthorizationRequestConverterStrategy
{
    private static boolean shouldSplitCryptogram(final String paymentCryptogram)
    {
        return paymentCryptogram != null && paymentCryptogram.getBytes().length == 40;
    }

    public boolean supports(final PaymentServiceRequest request)
    {
        return MERCHANT.equals(request.getRequiredParam(DECRYPTION_TYPE))
                && CardType.AMEX.equals(request.getRequiredParam(CARD_TYPE));
    }

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Map<String, Object> paymentToken = source.getRequiredParam(PAYMENT_TOKEN);
        final Map paymentData = (Map) paymentToken.get(PAYMENT_DATA);
        String paymentCryptogram = (String) paymentData.get(PAYMENT_CRYPTOGRAM);
        String xid = null;
        if (shouldSplitCryptogram(paymentCryptogram))
        {
            final byte[] paymentCryptogramBytes = paymentCryptogram.getBytes();
            paymentCryptogram = new String(Arrays.copyOf(paymentCryptogramBytes, 20));
            xid = new String(Arrays.copyOfRange(paymentCryptogramBytes, 20, 40));
        }

        return createMerchDecryptBaseRequest(source)
                .addParam(CC_AUTH_SERVICE_CAVV, paymentCryptogram)
                .addParam(CC_AUTH_SERVICE_XID, xid)
                .addParam(CC_AUTH_SERVICE_COMMERCE_INDICATOR, AMEX_COMMERCE_INDICATOR)
                .request();
    }
}
