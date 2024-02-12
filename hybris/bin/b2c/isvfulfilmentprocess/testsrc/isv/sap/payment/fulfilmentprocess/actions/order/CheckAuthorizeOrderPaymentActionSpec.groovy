package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

@UnitTest
class CheckAuthorizeOrderPaymentActionSpec extends Specification
{
    def process = Mock([useObjenesis: false], OrderProcessModel)

    def order = Mock([useObjenesis: false], OrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def authorizeTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def action = new CheckAuthorizeOrderPaymentAction()

    def setup()
    {
        action.modelService = modelService
        action.paymentTransactionService = paymentTransactionService

        process.order >> order
        order.paymentTransactions >> [transaction]
    }

    @Test
    def 'should return OK transaction if order has an ACCEPTED authorization'()
    {
        given:
        paymentTransactionService.getLatestTransactionEntry(
                transaction,
                PaymentTransactionType.AUTHORIZATION,
                IsvPaymentConstants.TransactionStatus.ACCEPT, IsvPaymentConstants.TransactionStatus.REVIEW
        ) >> Optional.of(authorizeTransactionEntry)

        when:
        def transition = action.executeAction(process)

        then:
        1 * order.setStatus(OrderStatus.PAYMENT_AUTHORIZED)
        1 * modelService.save(order)
        transition == AbstractSimpleDecisionAction.Transition.OK
    }

    @Test
    def 'should return NOK transaction if order has no ACCEPTED authorizations'()
    {
        given:
        paymentTransactionService.getLatestTransactionEntry(
                transaction,
                PaymentTransactionType.AUTHORIZATION,
                IsvPaymentConstants.TransactionStatus.ACCEPT, IsvPaymentConstants.TransactionStatus.REVIEW
        ) >> Optional.empty()

        when:
        def transition = action.executeAction(process)

        then:
        0 * order.setStatus(OrderStatus.PAYMENT_AUTHORIZED)
        0 * modelService.save(order)
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }
}
