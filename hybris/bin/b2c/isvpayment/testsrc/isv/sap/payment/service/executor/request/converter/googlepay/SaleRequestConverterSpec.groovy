package isv.sap.payment.service.executor.request.converter.googlepay

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.DefaultRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.GooglePay.SALE

@UnitTest
class SaleRequestConverterSpec extends Specification
{
    def requestFactory = Mock(RequestFactory)

    def googlePayAuthorizationRequestConverter = Mock(AuthorizationRequestConverter)

    def paymentTransaction = new PaymentTransaction()

    def source = PaymentServiceRequest.create()

    def converter = new SaleRequestConverter(
            googlePayAuthorizationRequestConverter: googlePayAuthorizationRequestConverter,
            requestFactory: requestFactory)

    def setup()
    {
        requestFactory.request(SALE) >> paymentTransaction
    }

    @Test
    def 'should convert source to request'()
    {
        given:
        def googlePayAuthRequest = new DefaultRequest()
        googlePayAuthRequest.addRequestField('auth', 'value')

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        1 * converter.googlePayAuthorizationRequestConverter.convert(source) >> googlePayAuthRequest
        requestFields.ccCaptureServiceRun == true
        requestFields.auth == 'value'
    }
}
