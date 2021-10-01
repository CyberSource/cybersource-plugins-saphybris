package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.CAPTURE

@UnitTest
class CaptureRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def source = new PaymentServiceRequest()

    def converter = new CaptureRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CAPTURE) >> paymentTransaction

        order.guid >> 'guid'
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 12D

        transactionEntry.requestId >> 'transactionEntry_requestId'

        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)
        source.addParam('vcOrderId', 'vcOrderId')
        source.addParam('merchantId', 'merchantId')
    }

    @Test
    def 'should populate specific fields'()
    {
        when:
        def target = converter.convert(source)

        and:
        def requestFields = target.requestFields

        then:
        requestFields['vcOrderId'] == 'vcOrderId'
        requestFields['merchantId'] == 'merchantId'
        requestFields['paymentSolution'] == 'visacheckout'
        requestFields['merchantReferenceCode'] == 'guid'
        requestFields['ccCaptureServiceRun'] == true
        requestFields['ccCaptureServiceAuthRequestID'] == 'transactionEntry_requestId'
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['purchaseTotalsGrandTotalAmount'] == 12
    }
}
