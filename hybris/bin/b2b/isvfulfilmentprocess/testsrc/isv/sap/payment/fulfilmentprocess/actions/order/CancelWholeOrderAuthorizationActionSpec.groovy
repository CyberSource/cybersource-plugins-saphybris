package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy
import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static de.hybris.platform.core.enums.OrderStatus.CANCELLED
import static de.hybris.platform.core.enums.OrderStatus.PROCESSING_ERROR
import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
import static de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REJECT

class CancelWholeOrderAuthorizationActionSpec extends Specification
{

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)
    def modelService = Mock([useObjenesis: false], ModelService)
    def context = Mock([useObjenesis: false], PaymentOperationContext)
    def authReversalStrategy = Mock([useObjenesis: false], PaymentOperationStrategy)

    def process = Mock([useObjenesis: false], OrderProcessModel)
    def order = Mock([useObjenesis: false], OrderModel)
    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def action = new CancelWholeOrderAuthorizationAction()

    def setup()
    {
        process.order >> order
        context.strategy(_) >> authReversalStrategy

        action.modelService = modelService
        action.paymentTransactionService = paymentTransactionService
        action.authorizationReversalContext = context
    }

    @Test
    def 'should cancel order if no transactions associated'()
    {
        given:
        order.paymentTransactions >> []

        when:
        action.executeAction(process)

        then:
        1 * order.setStatus(CANCELLED)
        0 * authReversalStrategy.execute(_, _)
    }

    @Test
    def 'should cancel order and reverse authorization'()
    {
        given:
        order.paymentTransactions >> [transaction]
        def authTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        def reversal = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        reversal.transactionStatus >> ACCEPT
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, CAPTURE) >> Optional.empty()
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION) >> Optional.of(authTransactionEntry)

        when:
        action.executeAction(process)

        then:
        1 * order.setStatus(CANCELLED)
        1 * authReversalStrategy.execute(order, transaction) >> reversal
    }

    @Test
    def 'should set PROCESING_ERROR status if capture transaction'()
    {
        given:
        order.paymentTransactions >> [transaction]
        def captureTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, CAPTURE) >> Optional.of(captureTransactionEntry)

        when:
        action.executeAction(process)

        then:
        1 * order.setStatus(PROCESSING_ERROR)
        0 * authReversalStrategy.execute(order, transaction)
    }

    @Test
    def 'should set PROCESSING_ERROR status if reverse authorization fails'()
    {
        given:
        order.paymentTransactions >> [transaction]
        def authTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        def reversal = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        reversal.transactionStatus >> REJECT
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, CAPTURE) >> Optional.empty()
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION) >> Optional.of(authTransactionEntry)

        when:
        action.executeAction(process)

        then:
        1 * order.setStatus(PROCESSING_ERROR)
        1 * authReversalStrategy.execute(order, transaction) >> reversal
    }

    @Test
    def 'should set PROCESSING_ERROR status if no strategy'()
    {
        given:
        order.paymentTransactions >> [transaction]
        def authTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, CAPTURE) >> Optional.empty()
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION) >> Optional.of(authTransactionEntry)

        when:
        action.executeAction(process)

        then:
        1 * order.setStatus(PROCESSING_ERROR)
        1 * authReversalStrategy.execute(order, transaction) >> { throw new IllegalArgumentException() }
    }
}
