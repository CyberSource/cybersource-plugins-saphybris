package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.fulfilmentprocess.CheckOrderService

@UnitTest
class CheckOrderActionSpec extends Specification
{
    def checkOrderService = Mock(CheckOrderService)

    def modelService = Mock(ModelService)

    def action = new CheckOrderAction(
            checkOrderService: checkOrderService,
            modelService: modelService
    )

    @Test
    def 'Should check order as valid'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)
        def order = Mock(OrderModel)
        orderProcess.order >> order

        when:
        def transition = action.executeAction(orderProcess)

        then:
        1 * checkOrderService.check(order) >> true
        1 * order.setStatus(OrderStatus.CHECKED_VALID)
        1 * modelService.save(order)
        transition == AbstractSimpleDecisionAction.Transition.OK
    }

    @Test
    def 'Should check order as invalid'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)
        def order = Mock(OrderModel)
        orderProcess.order >> order

        when:
        def transition = action.executeAction(orderProcess)

        then:
        1 * checkOrderService.check(order) >> false
        1 * order.setStatus(OrderStatus.CHECKED_INVALID)
        1 * modelService.save(order)
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }

    @Test
    def 'Should skip action when order is null'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)

        when:
        def transition = action.executeAction(orderProcess)

        then:
        0 * checkOrderService.check(_)
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }
}
