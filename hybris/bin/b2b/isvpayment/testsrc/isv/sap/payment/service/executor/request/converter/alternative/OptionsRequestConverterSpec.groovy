package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.OPTIONS
import static isv.cjl.payment.enums.AlternativePaymentMethod.IDL

@UnitTest
class OptionsRequestConverterSpec extends Specification
{
    def converter = new OptionsRequestConverter()

    def source = PaymentServiceRequest.create()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(OPTIONS) >> new PaymentTransaction()

        source.addParam('merchantId', 'merchantId')
    }

    @Test
    def 'should convert source to options request with order reference code'()
    {
        given:
        def order = Mock([useObjenesis: false], AbstractOrderModel)
        order.guid >> 'guid'

        source.addParam('order', order)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['apOptionsServiceRun'] == true
        target.requestFields['apPaymentType'] == IDL.code
    }

    @Test
    def 'should convert source to options request with random uuid'()
    {
        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] != null
        target.requestFields['apOptionsServiceRun'] == true
        target.requestFields['apPaymentType'] == IDL.code
    }
}
