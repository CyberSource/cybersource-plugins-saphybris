package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.warehouse.Process2WarehouseAdapter
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class AllowShipmentActionSpec extends Specification
{
    def process2WarehouseAdapter = Mock(Process2WarehouseAdapter)

    def action = new AllowShipmentAction(process2WarehouseAdapter: process2WarehouseAdapter)

    @Test
    @Unroll
    def 'Should return transition to cancel when order status is cancelled'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def order = Mock(OrderModel)
        order.status >> orderStatusParam
        def consignment = Mock(ConsignmentModel)
        consignment.order >> order
        consignmentProcess.consignment >> consignment

        when:
        def transition = action.execute(consignmentProcess)

        then:
        transition == 'CANCEL'

        where:
        orderStatusParam       | _
        OrderStatus.CANCELLED  | _
        OrderStatus.CANCELLING | _
    }

    @Test
    @Unroll
    def 'Should return transition based on consignment delivery mode'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def order = Mock(OrderModel)
        order.status >> OrderStatus.PAYMENT_CAPTURED
        def consignment = Mock(ConsignmentModel)
        consignment.order >> order
        consignment.deliveryMode >> deliveryModeParam
        consignmentProcess.consignment >> consignment

        when:
        def transition = action.execute(consignmentProcess)

        then:
        1 * process2WarehouseAdapter.shipConsignment(consignment)
        transition == expectedTransition

        where:
        deliveryModeParam             | expectedTransition
        Stub(PickUpDeliveryModeModel) | 'PICKUP'
        Stub(ZoneDeliveryModeModel)   | 'DELIVERY'
    }

    @Test
    def 'Should return transition to error when an exception occurs when shipping the consignment'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def order = Mock(OrderModel)
        order.status >> OrderStatus.PAYMENT_CAPTURED
        def consignment = Mock(ConsignmentModel)
        consignment.order >> order
        consignmentProcess.consignment >> consignment

        when:
        def transition = action.execute(consignmentProcess)

        then:
        1 * process2WarehouseAdapter.shipConsignment(consignment) >> { throw new Exception() }
        transition == 'ERROR'
    }

    @Test
    def 'Should return transition to error when consignment is null'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        def transition = action.execute(consignmentProcess)

        then:
        transition == 'ERROR'
    }
}
