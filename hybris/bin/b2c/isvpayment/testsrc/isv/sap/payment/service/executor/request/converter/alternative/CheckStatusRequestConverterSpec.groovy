package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.CHECK_STATUS
import static isv.cjl.payment.enums.AlternativePaymentMethod.IDL
import static isv.cjl.payment.enums.AlternativePaymentMethod.SOF
import static isv.cjl.payment.enums.PaymentTransactionType.INITIATE
import static isv.cjl.payment.enums.PaymentTransactionType.REFUND
import static isv.cjl.payment.enums.PaymentTransactionType.SALE

@UnitTest
class CheckStatusRequestConverterSpec extends Specification
{
    def converter = new CheckStatusRequestConverter()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(CHECK_STATUS) >> new PaymentTransaction()
        order.guid >> 'guid'
        transactionEntry.requestId >> 'AA00022'

        source.addParam('merchantId', 'merchantId')
    }

    @Test
    def 'should convert source to check status initiate request'()
    {
        given:
        transactionEntry.type = PaymentTransactionType.INITIATE
        source.addParam('paymentTransactionType', INITIATE)
        source.addParam('order', order)
        source.addParam('alternatePaymentMethod', AlternativePaymentMethod.APY)
        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['apCheckStatusServiceRun'] == true
        target.requestFields['apCheckStatusServiceInitiateRequestID'] == 'AA00022'
        target.requestFields['apPaymentType'] == AlternativePaymentMethod.APY.code
    }

    @Test
    def 'should convert source to check status sale request'()
    {
        given:
        transactionEntry.type = PaymentTransactionType.SALE
        source.addParam('paymentTransactionType', SALE)
        source.addParam('order', order)
        source.addParam('alternatePaymentMethod', AlternativePaymentMethod.IDL)
        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['apCheckStatusServiceRun'] == true
        target.requestFields['apCheckStatusServiceCheckStatusRequestID'] == 'AA00022'
        target.requestFields['apPaymentType'] == IDL.code
    }

    @Test
    def 'should convert source to check status refund request'()
    {
        given:
        transactionEntry.type = PaymentTransactionType.REFUND
        source.addParam('paymentTransactionType', REFUND)
        source.addParam('order', order)
        source.addParam('alternatePaymentMethod', AlternativePaymentMethod.SOF)
        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['apCheckStatusServiceRun'] == true
        target.requestFields['apCheckStatusServiceCheckStatusRequestID'] == 'AA00022'
        target.requestFields['apPaymentType'] == SOF.code
    }
}
