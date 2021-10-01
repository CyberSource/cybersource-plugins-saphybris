package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativeAuthorizeOrderPaymentAction.Transition.OK
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativeAuthorizeOrderPaymentAction.Transition.WAIT
import static java.util.Optional.empty
import static java.util.Optional.of

@UnitTest
class CheckAlternativeAuthorizeOrderPaymentActionSpec extends Specification
{
    def action = new CheckAlternativeAuthorizeOrderPaymentAction()

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def alternativePaymentOrderStatusService = Mock([useObjenesis: false], AlternativePaymentOrderStatusService)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def authEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def process = Mock([useObjenesis: false], OrderProcessModel)

    def 'setup'()
    {
        action.paymentTransactionService = paymentTransactionService
        action.alternativePaymentOrderStatusService = alternativePaymentOrderStatusService

        def order = Mock([useObjenesis: false], OrderModel)

        process.order >> order

        alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order) >> transaction
    }

    @Test
    def 'should check authorization status if corresponding entry exists'()
    {
        given:
        authEntry.properties >> ['paymentStatus': status]
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION) >> of(authEntry)

        when:
        def result = action.execute(process)

        then:
        result == transition.toString()

        where:
        status       | transition
        'AUTHORIZED' | OK
        'WHATEVER'   | WAIT
    }

    @Test
    def 'should not check authorization status if corresponding entry does not exist'()
    {
        given:
        paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION) >> empty()

        when:
        def result = action.execute(process)

        then:
        result == WAIT.toString()
    }
}
