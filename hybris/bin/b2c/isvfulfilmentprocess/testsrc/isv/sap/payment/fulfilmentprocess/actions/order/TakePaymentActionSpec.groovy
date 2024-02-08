package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
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

import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_CAPTURED
import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_NOT_CAPTURED
import static de.hybris.platform.core.enums.OrderStatus.PROCESSING_ERROR
import static de.hybris.platform.core.enums.OrderStatus.WAITING_FOR_PAYMENT
import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW
import static isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction.Transition.NOK
import static isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction.Transition.OK
import static isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction.Transition.WAIT
import static java.util.Optional.empty
import static java.util.Optional.of

@UnitTest
class TakePaymentActionSpec extends Specification
{
    def action = new TakePaymentAction()

    def transaction = new IsvPaymentTransactionModel()

    def captureEntry = new IsvPaymentTransactionEntryModel()

    def authEntry = new IsvPaymentTransactionEntryModel()

    def order = Mock([useObjenesis: false], OrderModel)

    def process = Mock([useObjenesis: false], OrderProcessModel)

    def paymentOperationContext = Mock([useObjenesis: false], PaymentOperationContext)

    def paymentOperationStrategy = Mock([useObjenesis: false], PaymentOperationStrategy)

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def 'setup'()
    {
        paymentOperationContext.strategy(*_) >> paymentOperationStrategy

        action.alternativePaymentOrderStatusMap = ['SETTLED': PAYMENT_CAPTURED, 'PENDING': WAITING_FOR_PAYMENT]
        action.orderStatusTransitionMap = [(PAYMENT_CAPTURED): OK, (WAITING_FOR_PAYMENT): WAIT, (PAYMENT_NOT_CAPTURED): NOK]

        action.paymentTransactionService = paymentTransactionService
        action.modelService = modelService
        action.paymentOperationContext = paymentOperationContext

        captureEntry.setPaymentTransaction(transaction)

        process.getOrder() >> order
    }

    @Test
    def 'should set order status to PROCESSING_ERROR and return NOK if no strategy available'()
    {
        given:
        order.setPaymentTransactions([transaction])
        paymentTransactionService.getLatestTransactionEntry(*_) >> of(authEntry)
        paymentOperationContext.strategy(transaction) >> {
            throw new IllegalArgumentException()
        }

        when:
        def result = action.execute(process)

        then:
        1 * order.setStatus(PROCESSING_ERROR)
        result == NOK.toString()
    }

    @Test
    def 'should perform capture and return OK'()
    {
        given:
        order.paymentTransactions >> [transaction, transaction]
        transaction.setPaymentProvider('CREDIT_CARD')
        captureEntry.setTransactionStatus('ACCEPT')

        when:
        def result = action.execute(process)

        then:
        result == OK.toString()
        2 * paymentTransactionService.getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW) >> of(authEntry)
        2 * paymentOperationStrategy.execute(order, transaction) >> captureEntry
        2 * order.setStatus(PAYMENT_CAPTURED)
    }

    @Test
    def 'should return NOK if capture is not successfull'()
    {
        given:
        def transaction2 = new IsvPaymentTransactionModel()
        order.paymentTransactions >> [transaction, transaction2]
        paymentTransactionService.getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW) >> of(authEntry)
        transaction.setPaymentProvider('CREDIT_CARD')
        captureEntry.setTransactionStatus('REJECT')

        when:
        def result = action.execute(process)

        then:
        result == NOK.toString()
        1 * paymentOperationStrategy.execute(order, transaction) >> captureEntry
        0 * paymentOperationStrategy.execute(order, transaction2)
        1 * order.setStatus(PAYMENT_NOT_CAPTURED)
    }

    @Test
    def 'should not update order status if no authorization'()
    {
        given:
        order.paymentTransactions >> [transaction]
        paymentTransactionService.getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW) >> empty()

        when:
        def result = action.execute(process)

        then:
        result == OK.toString()
        0 * order.setStatus(_)
    }

    @Test
    def 'should resolve order status from capture response'()
    {
        given:
        transaction.setPaymentProvider(paymentProvider)
        captureEntry.setTransactionStatus(transactionStatus)
        captureEntry.setProperties(['paymentStatus': paymentStatus])

        when:
        def result = action.getOrderStatusFromCapture(captureEntry)

        then:
        result == orderStatus

        where:
        transactionStatus | paymentStatus | paymentProvider       | orderStatus
        'ACCEPT'          | ''            | 'CREDIT_CARD'         | PAYMENT_CAPTURED
        'WHATEVER'        | ''            | 'CREDIT_CARD'         | PAYMENT_NOT_CAPTURED
        'ACCEPT'          | 'pending'     | 'PAY_PAL'             | WAITING_FOR_PAYMENT
        'ACCEPT'          | 'Settled'     | 'ALTERNATIVE_PAYMENT' | PAYMENT_CAPTURED
    }

    @Test
    def 'should throw exception if no order status mapping exists'()
    {
        given:
        transaction.setPaymentProvider('ALTERNATIVE_PAYMENT')
        captureEntry.setTransactionStatus('ACCEPT')
        captureEntry.setProperties(['paymentStatus': 'DNE'])

        when:
        action.getOrderStatusFromCapture(captureEntry)

        then:
        thrown IllegalArgumentException
    }
}
