package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.REFUND
import static isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT

@UnitTest
class RefundRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def source = new PaymentServiceRequest()

    def converter = new RefundRequestConverter()

    def processingLevelPopulator = Mock([useObjenesis: false], isv.cjl.payment.service.executor.request.populator.Populator)

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(REFUND) >> paymentTransaction

        order.guid >> 'order_guid'
        order.currency >> [isocode: 'USD']

        source.addParam('order', order)
        source.addParam('amount', 12.34)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('vcOrderId', '123456')
        source.paymentType = VISA_CHECKOUT

        converter.processingLevelPopulator = processingLevelPopulator
    }

    @Test
    def 'should create refund request'()
    {
        given:
        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.merchantId == 'merchant_identifier'
        requestFields.purchaseTotalsGrandTotalAmount == 12.34
        requestFields.merchantReferenceCode == 'order_guid'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.ccCreditServiceRun == true
        requestFields.vcOrderId == '123456'
        requestFields.paymentSolution == 'visacheckout'
    }
}
