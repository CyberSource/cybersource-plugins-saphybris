package isv.sap.payment.fulfilmentprocess.strategy.impl.refund

import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.enums.AlternativePaymentMethod.WQR
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static java.util.Optional.empty
import static java.util.Optional.of

class WeChatPayRefundStrategySpec extends Specification
{
    def paymentTransactionService = Mock(PaymentTransactionService)

    def strategy = new WeChatPayRefundStrategy(paymentTransactionService: paymentTransactionService)

    @Test
    def 'should return alternative payment payment type'()
    {
        expect:
        strategy.paymentType == PaymentType.ALTERNATIVE_PAYMENT
    }

    @Test
    def 'should return alternative payment method'()
    {
        expect:
        strategy.paymentMethod == WQR
    }

    @Test
    def 'should create we chat pay refund request'()
    {
        given:
        def totalAmount = BigDecimal.TEN
        def order = new OrderModel(totalPrice: totalAmount)
        def transaction = new IsvPaymentTransactionModel(merchantId: 'mid')
        def transactionEntry = Mock(IsvPaymentTransactionEntryModel)

        and:
        strategy.paymentTransactionService.getTransactionCardTypeNew(transaction) >> empty()
        paymentTransactionService.getLatestTransactionEntry(transaction, PaymentTransactionType.SALE, ACCEPT) >> of(transactionEntry)

        when:
        def refundRequest = strategy.request(order, transaction)
        def params = refundRequest.requestParams

        then:
        refundRequest.paymentType == PaymentType.ALTERNATIVE_PAYMENT
        refundRequest.paymentTransactionType.code == PaymentTransactionType.REFUND.code

        params.merchantId == transaction.merchantId
        params.alternatePaymentMethod == WQR
        params.order == order
        params.amount == totalAmount
        params.transaction == transactionEntry
    }
}
