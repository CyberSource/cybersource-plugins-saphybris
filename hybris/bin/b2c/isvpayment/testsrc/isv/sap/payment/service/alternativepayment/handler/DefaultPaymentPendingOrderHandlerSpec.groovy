package isv.sap.payment.service.alternativepayment.handler

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentType
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel

@UnitTest
class DefaultPaymentPendingOrderHandlerSpec extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def handler = new DefaultPaymentPendingOrderHandler()

    def setup()
    {
        transaction.merchantId >> 'hybris_test'
        transaction.alternativePaymentMethod >> AlternativePaymentMethod.KLI
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
    }

    @Test
    def 'Should create alternative (Klarna) check status service request'()
    {
        when:
        def request = handler.createCheckStatusRequestBuilder(order, transaction, transactionEntry)

        then:
        request.paymentType == PaymentType.ALTERNATIVE_PAYMENT
        request.paymentTransactionType == isv.cjl.payment.enums.PaymentTransactionType.CHECK_STATUS
        request.requestParams.merchantId == 'hybris_test'
        request.requestParams.order == order
        request.requestParams.transaction == transactionEntry
        request.requestParams.alternatePaymentMethod == isv.cjl.payment.enums.AlternativePaymentMethod.KLI
        request.requestParams.paymentTransactionType == isv.cjl.payment.enums.PaymentTransactionType.CHECK_STATUS
    }
}
