package isv.sap.payment.service.alternativepayment.handler

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel

@UnitTest
class PayPalPaymentPendingOrderHandlerSpec extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def handler = new PayPalPaymentPendingOrderHandler()

    def setup()
    {
        transaction.merchantId >> 'hybris_test'
    }

    @Test
    def 'Should create paypal check status service request'()
    {
        when:
        def request = handler.createCheckStatusRequestBuilder(order, transaction, transactionEntry)

        then:
        request.paymentType == PaymentType.PAY_PAL
        request.paymentTransactionType == PaymentTransactionType.CHECK_STATUS
        request.requestParams.apPaymentType == 'PPL'
        request.requestParams.merchantId == 'hybris_test'
        request.requestParams.order == order
        request.requestParams.transaction == transactionEntry
    }
}
