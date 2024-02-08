package isv.sap.payment.service.executor.request.converter.googlepay;

import java.math.BigDecimal;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.codec.binary.Base64;

import isv.cjl.payment.constants.PaymentRequestParamConstants;
import isv.cjl.payment.constants.PaymentServiceConstants;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.PaymentHelper;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Google Pay payment Authorization request.
 */
public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    private static final String PAYMENT_METHOD_DATA = "paymentMethodData";

    private static final String INFO = "info";

    private static final String CARD_NETWORK = "cardNetwork";

    private static final String TOKENIZATION_DATA = "tokenizationData";

    private static final String TOKEN = "token";

    @Inject
    @Named("isv.cjl.payment.utils.paymentHelper")
    private PaymentHelper paymentHelper;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final AddressModel billingAddress = order.getPaymentInfo().getBillingAddress();
        notNull(billingAddress, "billingAddress is required in the order");

        final Map<String, Object> paymentData = source.getRequiredParam(PaymentRequestParamConstants.PAYMENT_DATA);
        final Map<String, Object> paymentMethodData = PaymentParamUtils.getParam(PAYMENT_METHOD_DATA, paymentData);
        final Map<String, Object> info = PaymentParamUtils.getParam(INFO, paymentMethodData);
        final Map<String, Object> tokenizationData = PaymentParamUtils.getParam(TOKENIZATION_DATA, paymentMethodData);

        final String cardNetwork = PaymentParamUtils.getParam(CARD_NETWORK, info);
        final String token = PaymentParamUtils.getParam(TOKEN, tokenizationData);
        final String encryptedPaymentToken = new String(Base64.encodeBase64(token.getBytes()));

        return requestFactory.request(PaymentServiceConstants.GooglePay.AUTHORIZATION)
                .addParam(PaymentRequestParamConstants.MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PaymentRequestParamConstants.BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(PaymentRequestParamConstants.BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(PaymentRequestParamConstants.BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(PaymentRequestParamConstants.BILL_TO_STREET2, billingAddress.getLine2())
                .addParam(PaymentRequestParamConstants.BILL_TO_CITY, billingAddress.getTown())
                .addParam(PaymentRequestParamConstants.BILL_TO_STATE,
                        billingAddress.getRegion() == null ? null : billingAddress.getRegion().getIsocodeShort())
                .addParam(PaymentRequestParamConstants.BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(PaymentRequestParamConstants.BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(PaymentRequestParamConstants.BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(PaymentRequestParamConstants.BILL_TO_PHONE_NUMBER, billingAddress.getPhone1())
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT,
                        BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(PaymentRequestParamConstants.ENCRYPTED_PAYMENT_DATA, encryptedPaymentToken)
                .addParam(PaymentRequestParamConstants.CARD_TYPE, paymentHelper.getCardType(cardNetwork))
                .addParam(PaymentRequestParamConstants.CC_AUTH_SERVICE_RUN, true)
                .addParam(PaymentRequestParamConstants.PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE,
                        PaymentRequestParamConstants.GOOGLE_PAY_PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE_VALUE)
                .addParam(PaymentRequestParamConstants.PAYMENT_SOLUTION,
                        PaymentRequestParamConstants.GOOGLE_PAY_PAYMENT_SOLUTION_VALUE)
                .request();
    }
}
