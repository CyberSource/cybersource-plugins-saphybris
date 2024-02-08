package isv.sap.payment.fulfilmentprocess.actions.returns

import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.returns.model.ReturnEntryModel
import de.hybris.platform.returns.model.ReturnProcessModel
import de.hybris.platform.returns.model.ReturnRequestModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy
import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSAL_FAILED
import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSED
import static de.hybris.platform.payment.enums.PaymentTransactionType.REFUND_FOLLOW_ON
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.NOK
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REJECT

class CaptureRefundActionSpec extends Specification
{

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)
    def modelService = Mock([useObjenesis: false], ModelService)
    def context = Mock([useObjenesis: false], PaymentOperationContext)
    def refundStrategy = Mock([useObjenesis: false], PaymentOperationStrategy)

    def process = Mock([useObjenesis: false], ReturnProcessModel)
    def order = Mock([useObjenesis: false], OrderModel)
    def returnRequest = Mock([useObjenesis: false], ReturnRequestModel)
    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)
    def returnEntry = Mock(ReturnEntryModel)

    def action = new CaptureRefundAction()

    def setup()
    {
        process.order >> order
        process.returnRequest >> returnRequest
        returnRequest.getOrder() >> order
        returnRequest.returnEntries >> [returnEntry]

        context.strategy(_) >> refundStrategy

        action.modelService = modelService
        action.paymentTransactionService = paymentTransactionService
        action.paymentOperationContext = context
    }

    @Test
    def 'Should refund order using different refund types'()
    {
        given:
        order.paymentTransactions >> [transaction, transaction]
        def secondRefundStrategy = Mock([useObjenesis: false], PaymentOperationStrategy)
        paymentTransactionService.getLatestTransactionEntry(transaction, REFUND_FOLLOW_ON, ACCEPT) >> Optional.empty()

        def refund = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        refund.transactionStatus >> ACCEPT

        when:
        def result = action.executeAction(process)

        then:
        2 * context.strategy(_) >> refundStrategy >> secondRefundStrategy
        1 * refundStrategy.execute(order, transaction) >> refund
        1 * secondRefundStrategy.execute(order, transaction) >> refund
        1 * returnRequest.setStatus(PAYMENT_REVERSED)
        1 * returnEntry.setStatus(PAYMENT_REVERSED)
        result == OK
    }

    @Test
    def 'Should fail refund order if at least one refund failed'()
    {
        given:
        order.paymentTransactions >> [transaction, transaction]
        def secondRefundStrategy = Mock([useObjenesis: false], PaymentOperationStrategy)
        paymentTransactionService.getLatestTransactionEntry(transaction, REFUND_FOLLOW_ON, ACCEPT) >> Optional.empty()

        def refund = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        refund.transactionStatus >> ACCEPT

        def refund2 = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        refund.transactionStatus >> REJECT

        when:
        def result = action.executeAction(process)

        then:
        2 * context.strategy(_) >> refundStrategy >> secondRefundStrategy
        1 * refundStrategy.execute(order, transaction) >> refund
        1 * secondRefundStrategy.execute(order, transaction) >> refund2
        1 * returnRequest.setStatus(PAYMENT_REVERSAL_FAILED)
        result == NOK
    }

    @Test
    def 'Should not refund transactions already refunded'()
    {
        given:
        order.paymentTransactions >> [transaction]
        paymentTransactionService.getLatestTransactionEntry(transaction, REFUND_FOLLOW_ON, ACCEPT) >> Optional.of(transaction)

        when:
        def result = action.executeAction(process)

        then:
        0 * refundStrategy.execute(order, transaction)
        1 * returnRequest.setStatus(PAYMENT_REVERSED)
        result == OK
    }

    @Test
    def 'Should set PAYMENT_REVERSAL_FAILED status if error'()
    {
        given:
        order.paymentTransactions >> [transaction]
        paymentTransactionService.getLatestTransactionEntry(transaction, REFUND_FOLLOW_ON, ACCEPT) >> { throw new Exception() }

        when:
        def result = action.executeAction(process)

        then:
        1 * returnRequest.setStatus(PAYMENT_REVERSAL_FAILED)
        result == NOK
    }
}
