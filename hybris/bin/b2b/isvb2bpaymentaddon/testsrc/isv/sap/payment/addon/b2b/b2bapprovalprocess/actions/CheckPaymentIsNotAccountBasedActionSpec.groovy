package isv.sap.payment.addon.b2b.b2bapprovalprocess.actions

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.action.approval.CheckPaymentIsNotAccountBasedAction

@UnitTest
class CheckPaymentIsNotAccountBasedActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new CheckPaymentIsNotAccountBasedAction(modelService: modelService)

    def process = Mock(B2BApprovalProcessModel)

    def order = Mock(OrderModel)

    def setup()
    {
        process.order >> order
    }

    @Test
    def 'executeAction: Should pass if paymentType is not ACCOUNT'()
    {
        given:
        order.paymentType >> CheckoutPaymentType.CARD

        expect:
        action.executeAction(process) == AbstractSimpleDecisionAction.Transition.OK
    }

    @Test
    def 'executeAction: Should not pass if paymentType is ACCOUNT'()
    {
        given:
        order.paymentType >> CheckoutPaymentType.ACCOUNT

        expect:
        action.executeAction(process) == AbstractSimpleDecisionAction.Transition.NOK
    }

    @Test
    def 'executeAction: Should mark order with error status if an exception occurs'()
    {
        given:
        order.paymentType >> { throw new Exception('Error occurred') }

        when:
        def transition = action.executeAction(process)

        then:
        1 * order.setStatus(OrderStatus.B2B_PROCESSING_ERROR)
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }
}
