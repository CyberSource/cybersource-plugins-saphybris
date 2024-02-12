package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW
import static java.util.Optional.empty
import static java.util.Optional.of

@UnitTest
class ApplePayTakePaymentStrategySpec extends Specification
{
    def paymentTransactionService = Mock(PaymentTransactionService)

    def strategy = new ApplePayTakePaymentStrategy(paymentTransactionService: paymentTransactionService)

    @Test
    def 'should return apple pay payment type'()
    {
        expect:
        strategy.paymentType == PaymentType.APPLE_PAY
    }

    @Test
    def 'should create apple pay capture request without processing level data'()
    {
        given:
        def order = new OrderModel()
        def transaction = new IsvPaymentTransactionModel(merchantId: 'mid')
        def transactionEntry = Mock(IsvPaymentTransactionEntryModel)

        and:
        strategy.paymentTransactionService.getTransactionCardTypeNew(transaction) >> empty()
        paymentTransactionService.getLatestTransactionEntry(transaction, de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION, ACCEPT, REVIEW) >> of(transactionEntry)

        when:
        def capture = strategy.request(order, transaction)
        def params = capture.requestParams

        then:
        capture.paymentType == PaymentType.APPLE_PAY
        capture.paymentTransactionType == PaymentTransactionType.CAPTURE

        params.merchantId == transaction.merchantId
        params.order == order
        params.transaction == transactionEntry

        and: 'processing level data is not included within request'
        params.processingLevel == null
        params.paymentProcessor == null
    }
}
