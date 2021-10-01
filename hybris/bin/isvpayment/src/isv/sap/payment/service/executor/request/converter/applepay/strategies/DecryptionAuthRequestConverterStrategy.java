
package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.codec.binary.Base64;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.PaymentHelper;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_TOKEN_NETWORK;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_TOKEN_PAYMENT_DATA;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_TOKEN_PAYMENT_METHOD;
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.PAYMENT_TOKEN_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CARD_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DECRYPTION_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.ENCRYPTED_PAYMENT_DATA;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.ENCRYPTED_PAYMENT_DESCRIPTOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.ENCRYPTED_PAYMENT_ENCODING;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_TOKEN;
import static isv.cjl.payment.enums.DecryptionType.ISV_PAYMENT;

public class DecryptionAuthRequestConverterStrategy extends AbstractAuthorizationRequestConverterStrategy
{
    @Inject
    @Named("isv.cjl.payment.utils.paymentHelper")
    private PaymentHelper paymentHelper;

    public boolean supports(final PaymentServiceRequest request)
    {
        return ISV_PAYMENT.equals(request.getRequiredParam(DECRYPTION_TYPE));
    }

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Map<String, Object> paymentToken = source.getRequiredParam(PAYMENT_TOKEN);
        final Map<String, Object> token = PaymentParamUtils.getParam(PAYMENT_TOKEN_TOKEN, paymentToken);
        final Map<String, Object> paymentMethod = PaymentParamUtils.getParam(PAYMENT_TOKEN_PAYMENT_METHOD, token);
        final Map<String, Object> paymentData = PaymentParamUtils.getParam(PAYMENT_TOKEN_PAYMENT_DATA, token);
        final String data = new String(Base64.encodeBase64(new Gson().toJson(paymentData).getBytes()));
        final String network = PaymentParamUtils.getParam(PAYMENT_TOKEN_NETWORK, paymentMethod);

        return createBaseRequest(source)
                .addParam(CARD_TYPE, paymentHelper.getCardType(network))
                .addParam(ENCRYPTED_PAYMENT_DATA, data)
                .addParam(ENCRYPTED_PAYMENT_DESCRIPTOR, Authorization.ENCRYPTED_PAYMENT_DESCRIPTOR)
                .addParam(ENCRYPTED_PAYMENT_ENCODING, Authorization.PAYMENT_ENCODING)
                .addParam(PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE, Authorization.PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE)
                .request();
    }
}
