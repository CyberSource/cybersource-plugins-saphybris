package isv.sap.payment.service.executor.request.converter.creditcard;

import java.util.Map;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_RESPONSE;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.SA_AUTHORIZATION;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment authorization request.
 */
public class SaAuthorizationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Map<String, String> paymentResponse = source.getRequiredParam(PAYMENT_RESPONSE);

        return requestFactory.request(SA_AUTHORIZATION)
                .addParam(PAYMENT_RESPONSE, paymentResponse)
                .request();
    }
}
