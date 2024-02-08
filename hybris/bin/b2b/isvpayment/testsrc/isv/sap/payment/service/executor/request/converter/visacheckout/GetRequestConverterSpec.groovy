package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.GET

@UnitTest
class GetRequestConverterSpec extends Specification
{
    def converter = new GetRequestConverter()

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(GET) >> paymentTransaction

        source.addParam('merchantId', 'merchantIdentifier')
        source.addParam('vcOrderId', 'visaCheckoutOrderIdentifier')
    }

    @Test
    def 'should convert source to visa checkout get data request'()
    {
        def order = Mock([useObjenesis: false], AbstractOrderModel)
        order.guid >> 'guid'

        given:
        source.addParam('order', order)

        when:
        def reqFields = converter.convert(source).requestFields

        then:
        reqFields['getVisaCheckoutDataServiceRun'] == true
        reqFields['merchantId'] == 'merchantIdentifier'
        reqFields['merchantReferenceCode'] == 'guid'
        reqFields['paymentSolution'] == 'visacheckout'
        reqFields['vcOrderId'] == 'visaCheckoutOrderIdentifier'
    }
}
