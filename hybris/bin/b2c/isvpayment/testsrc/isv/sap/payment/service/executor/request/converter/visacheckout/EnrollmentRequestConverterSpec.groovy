package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.ENROLLMENT
import static java.math.BigDecimal.TEN

@UnitTest
class EnrollmentRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def source = PaymentServiceRequest.create()

    def converter = new EnrollmentRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(ENROLLMENT) >> paymentTransaction

        order.guid >> '123'
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 10

        source.addParam('vcOrderId', '123456')
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
    }

    @Test
    def 'converter should create and populate VC enrollment request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['ccAuthServiceRun'] == true
        requestFields['vcOrderId'] == '123456'
        requestFields['paymentSolution'] == 'visacheckout'
        requestFields['merchantId'] == 'merchant_identifier'
        requestFields['merchantReferenceCode'] == '123'
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['payerAuthEnrollServiceRun'] == true
        requestFields['purchaseTotalsGrandTotalAmount'] == TEN
    }
}
