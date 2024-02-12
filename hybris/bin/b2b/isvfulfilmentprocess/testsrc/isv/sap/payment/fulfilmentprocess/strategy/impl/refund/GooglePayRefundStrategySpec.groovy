package isv.sap.payment.fulfilmentprocess.strategy.impl.refund

import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static java.util.Optional.empty
import static java.util.Optional.of

class GooglePayRefundStrategySpec extends Specification
{
    def paymentTransactionService = Mock(PaymentTransactionService)

    def strategy = new GooglePayRefundStrategy(paymentTransactionService: paymentTransactionService)

    @Test
    def 'should return google pay payment type'()
    {
        expect:
        strategy.paymentType == PaymentType.GOOGLE_PAY
    }

    @Test
    def 'should create google pay refund request'()
    {
        given:
        def order = new OrderModel()
        def transaction = new IsvPaymentTransactionModel(merchantId: 'mid')
        def transactionEntry = Mock(IsvPaymentTransactionEntryModel)

        and:
        strategy.paymentTransactionService.getTransactionCardTypeNew(transaction) >> empty()
        paymentTransactionService.getLatestTransactionEntry(transaction, de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE, ACCEPT) >> of(transactionEntry)

        when:
        def refundRequest = strategy.request(order, transaction)
        def params = refundRequest.requestParams

        then:
        refundRequest.paymentType == PaymentType.GOOGLE_PAY
        refundRequest.paymentTransactionType == PaymentTransactionType.REFUND_FOLLOW_ON

        params.merchantId == transaction.merchantId
        params.order == order
        params.transaction == transactionEntry
    }
}
