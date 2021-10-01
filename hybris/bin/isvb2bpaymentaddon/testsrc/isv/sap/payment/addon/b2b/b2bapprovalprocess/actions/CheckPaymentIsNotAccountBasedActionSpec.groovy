package isv.sap.payment.addon.b2b.b2bapprovalprocess.actions

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.action.approval.CheckPaymentIsNotAccountBasedAction

@UnitTest
class CheckPaymentIsNotAccountBasedActionSpec extends Specification
{
    def action = new CheckPaymentIsNotAccountBasedAction()

    def process = new B2BApprovalProcessModel()

    def order = new OrderModel()

    def setup()
    {
        process.order = order
    }

    @Test
    def 'executeAction: Should pass if paymentType is not ACCOUNT'()
    {
        given:
        order.paymentType = CheckoutPaymentType.CARD

        expect:
        action.executeAction(process) == AbstractSimpleDecisionAction.Transition.OK
    }

    @Test
    def 'executeAction: Should not pass if paymentType is ACCOUNT'()
    {
        given:
        order.paymentType = CheckoutPaymentType.ACCOUNT

        expect:
        action.executeAction(process) == AbstractSimpleDecisionAction.Transition.NOK
    }
}
