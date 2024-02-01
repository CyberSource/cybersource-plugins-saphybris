package isv.sap.payment.service.executor.request.converter.applepay

import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.exception.PaymentException
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.service.executor.request.converter.applepay.strategies.AuthorizationRequestConverterStrategy

class AuthorizationRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationRequestConverter()

    def strategy1 = Mock([useObjenesis: false], AuthorizationRequestConverterStrategy)
    def strategy2 = Mock([useObjenesis: false], AuthorizationRequestConverterStrategy)

    void setup()
    {

        converter.strategies = [strategy1, strategy2]
    }

    @Test
    def 'should convert source using valid strategy'()
    {
        when:
        converter.convert(source)

        then:
        1 * strategy1.supports(source) >> false
        1 * strategy2.supports(source) >> true
        0 * strategy1.convert(source)
        1 * strategy2.convert(source)
    }

    @Test
    def 'should throw exception if strategy not found'()
    {
        when:
        converter.convert(source).requestFields

        then:
        1 * strategy1.supports(source) >> false
        1 * strategy2.supports(source) >> false
        0 * strategy1.convert(source)
        0 * strategy2.convert(source)
        def exception = thrown(PaymentException)
        exception.message.startsWith('Unable to find conversion strategy for')
    }
}
