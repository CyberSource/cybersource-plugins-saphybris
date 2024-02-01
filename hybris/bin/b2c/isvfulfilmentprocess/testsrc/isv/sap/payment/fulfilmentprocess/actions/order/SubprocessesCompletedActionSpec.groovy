package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.DeliveryStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SubprocessesCompletedActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new SubprocessesCompletedAction(
            modelService: modelService
    )

    @Test
    def 'Should mark order as not shipped when all consignments are in process'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)
        def consignmentProcessOne = Mock(ConsignmentProcessModel)
        consignmentProcessOne.done >> false
        def consignmentProcessTwo = Mock(ConsignmentProcessModel)
        consignmentProcessTwo.done >> false
        orderProcess.consignmentProcesses >> [consignmentProcessOne, consignmentProcessTwo]

        and:
        def order = Mock(OrderModel)
        orderProcess.order >> order

        when:
        def transition = action.executeAction(orderProcess)

        then:
        1 * order.setDeliveryStatus(DeliveryStatus.NOTSHIPPED)
        1 * modelService.save(order)

        and:
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }

    @Test
    def 'Should mark order as partially shipped when at least one consignment is in process'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)
        def consignmentProcessOne = Mock(ConsignmentProcessModel)
        consignmentProcessOne.done >> false
        def consignmentProcessTwo = Mock(ConsignmentProcessModel)
        consignmentProcessTwo.done >> true
        orderProcess.consignmentProcesses >> [consignmentProcessOne, consignmentProcessTwo]

        and:
        def order = Mock(OrderModel)
        orderProcess.order >> order

        when:
        def transition = action.executeAction(orderProcess)

        then:
        1 * order.setDeliveryStatus(DeliveryStatus.PARTSHIPPED)
        1 * modelService.save(order)

        and:
        transition == AbstractSimpleDecisionAction.Transition.NOK
    }

    @Test
    def 'Should mark order as shipped when all consignment are complete'()
    {
        given:
        def orderProcess = Mock(OrderProcessModel)
        def consignmentProcessOne = Mock(ConsignmentProcessModel)
        consignmentProcessOne.done >> true
        def consignmentProcessTwo = Mock(ConsignmentProcessModel)
        consignmentProcessTwo.done >> true
        orderProcess.consignmentProcesses >> [consignmentProcessOne, consignmentProcessTwo]

        and:
        def order = Mock(OrderModel)
        orderProcess.order >> order

        when:
        def transition = action.executeAction(orderProcess)

        then:
        1 * order.setDeliveryStatus(DeliveryStatus.SHIPPED)
        1 * modelService.save(order)

        and:
        transition == AbstractSimpleDecisionAction.Transition.OK
    }
}
