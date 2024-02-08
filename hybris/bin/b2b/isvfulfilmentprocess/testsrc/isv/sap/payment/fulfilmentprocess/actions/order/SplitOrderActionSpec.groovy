package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderEntryModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.ordersplitting.OrderSplittingService
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.processengine.BusinessProcessService
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SplitOrderActionSpec extends Specification
{
    def orderSplittingService = Mock(OrderSplittingService)

    def businessProcessService = Mock(BusinessProcessService)

    def modelService = Mock(ModelService)

    def action = new SplitOrderAction(
            orderSplittingService: orderSplittingService,
            businessProcessService: businessProcessService,
            modelService: modelService
    )

    @Test
    def 'Should split order into consignments'()
    {
        given:
        def order = Mock(OrderModel)
        def orderEntry = Mock(OrderEntryModel)
        order.entries >> [orderEntry]
        def orderProcess = Mock(OrderProcessModel)
        orderProcess.order >> order

        and:
        def consignment = Mock(ConsignmentModel)
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        action.executeAction(orderProcess)

        then:
        1 * orderSplittingService.splitOrderForConsignment(order, [orderEntry]) >> [consignment]
        1 * businessProcessService.createProcess(_, _) >> consignmentProcess

        and:
        consignmentProcess.setParentProcess(orderProcess)
        consignmentProcess.setConsignment(consignment)
        1 * modelService.save(consignmentProcess)

        and:
        1 * order.setStatus(OrderStatus.ORDER_SPLIT)
        1 * modelService.save(order)
    }

    @Test
    def 'Should not split order if entries already have a consignment'()
    {
        given:
        def order = Mock(OrderModel)
        def orderEntry = Mock(OrderEntryModel)
        orderEntry.consignmentEntries >> [Mock(ConsignmentEntryModel)]
        order.entries >> [orderEntry]
        def orderProcess = Mock(OrderProcessModel)
        orderProcess.order >> order

        when:
        action.executeAction(orderProcess)

        then:
        1 * orderSplittingService.splitOrderForConsignment(order, []) >> []
        0 * businessProcessService.createProcess(_, _)

        and:
        1 * order.setStatus(OrderStatus.ORDER_SPLIT)
        1 * modelService.save(order)
    }
}
