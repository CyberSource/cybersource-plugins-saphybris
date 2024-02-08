package isv.sap.payment.addon.b2b.fulfilmentprocess.actions

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.service.PaymentTransactionService

@UnitTest
class B2BCheckAuthorizeOrderPaymentActionSpec extends Specification
{
    def action = new B2BCheckAuthorizeOrderPaymentAction()

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def order = new OrderModel()

    def 'setup'()
    {
        action.modelService = modelService
        action.paymentTransactionService = paymentTransactionService
    }

    @Test
    def 'assignStatusForOrder: should pass order if it was paid by ACCOUNT'()
    {
        given:
        order.paymentType = CheckoutPaymentType.ACCOUNT

        when:
        def result = action.assignStatusForOrder(order)

        then:
        result == AbstractSimpleDecisionAction.Transition.OK
        order.status == OrderStatus.PAYMENT_AUTHORIZED
    }

    @Test
    def 'assignStatusForOrder: should keep default behaviour for payment types other than ACCOUNT'()
    {
        given:
        order.paymentType = CheckoutPaymentType.CARD
        order.paymentTransactions = []

        when:
        def result = action.assignStatusForOrder(order)

        then:
        result == AbstractSimpleDecisionAction.Transition.NOK
        order.status == null
    }
}
