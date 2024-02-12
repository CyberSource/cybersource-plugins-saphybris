package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

public interface AuthorizationRequestConverterStrategy
{
    boolean supports(PaymentServiceRequest request);

    Request convert(final PaymentServiceRequest source);
}
