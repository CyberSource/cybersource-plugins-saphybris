package isv.sap.payment.service.executor.request.converter.googlepay;

import javax.annotation.Resource;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_RUN;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Google Pay payment Sale request.
 */
public class SaleRequestConverter extends AbstractRequestConverter
{
    @Resource(name = "isv.sap.payment.service.executor.request.converter.googlepay.authorizationRequestConverter")
    private AuthorizationRequestConverter googlePayAuthorizationRequestConverter;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Request request = googlePayAuthorizationRequestConverter.convert(source);
        request.addRequestField(CC_CAPTURE_SERVICE_RUN, true);

        return request;
    }

    public void setGooglePayAuthorizationRequestConverter(
            final AuthorizationRequestConverter googlePayAuthorizationRequestConverter)
    {
        this.googlePayAuthorizationRequestConverter = googlePayAuthorizationRequestConverter;
    }
}
