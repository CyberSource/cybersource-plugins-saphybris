package isv.sap.payment.service.executor.request.converter.applepay

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.DefaultRequest
import isv.cjl.payment.service.request.RequestFactory

@UnitTest
class SaleRequestConverterSpec extends Specification
{
    def applePayAuthorizationRequestConverter = Mock(AuthorizationRequestConverter)

    def requestFactory = Mock(RequestFactory)

    def converter = new SaleRequestConverter(
            applePayAuthorizationRequestConverter: applePayAuthorizationRequestConverter,
            requestFactory: requestFactory
    )

    @Test
    def 'Should convert payment service request'()
    {
        given:
        def paymentServiceRequest = Mock(PaymentServiceRequest)
        def applePayAuthRequest = new DefaultRequest()

        when:
        def request = converter.convert(paymentServiceRequest)

        then:
        1 * applePayAuthorizationRequestConverter.convert(paymentServiceRequest) >> applePayAuthRequest
        request.requestFields['ccCaptureServiceRun'] == true
    }
}
