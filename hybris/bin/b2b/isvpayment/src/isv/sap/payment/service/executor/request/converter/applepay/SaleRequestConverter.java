package isv.sap.payment.service.executor.request.converter.applepay;

import javax.annotation.Resource;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_RUN;

public class SaleRequestConverter extends AbstractRequestConverter
{
    @Resource(name = "isv.sap.payment.service.executor.request.converter.applepay.authorizationRequestConverter")
    private AuthorizationRequestConverter applePayAuthorizationRequestConverter;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Request request = applePayAuthorizationRequestConverter.convert(source);
        request.addRequestField(CC_CAPTURE_SERVICE_RUN, true);

        return request;
    }
}
