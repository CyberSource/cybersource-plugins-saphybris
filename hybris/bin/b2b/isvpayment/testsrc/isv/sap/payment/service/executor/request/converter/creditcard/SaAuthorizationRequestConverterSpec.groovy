package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.SA_AUTHORIZATION

@UnitTest
class SaAuthorizationRequestConverterSpec extends Specification
{
    def converter = new SaAuthorizationRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(SA_AUTHORIZATION) >> paymentTransaction
    }

    @Test
    def 'should create request and set required parameters'()
    {
        given:
        def paymentResponse = [decision: 'ACCEPT']
        def source = PaymentServiceRequest.create().addParam('paymentResponse', paymentResponse)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['paymentResponse'] == paymentResponse
    }
}
