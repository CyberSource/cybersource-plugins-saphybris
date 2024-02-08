package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.delivery.dao.PickupDeliveryModeDao
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.storelocator.model.PointOfServiceModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SplitByDeliveryModeSpec extends Specification
{
    def pickupDeliveryModeDao = Mock(PickupDeliveryModeDao)

    def strategy = new SplitByDeliveryMode(
            pickupDeliveryModeDao: pickupDeliveryModeDao
    )

    @Test
    def 'Should return delivery mode from entry as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def deliveryMode = Stub(DeliveryModeModel)
        orderEntry.deliveryMode >> deliveryMode

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == deliveryMode
    }

    @Test
    def 'Should return delivery mode from order as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def order = Mock(OrderModel)
        def deliveryMode = Stub(DeliveryModeModel)
        order.deliveryMode >> deliveryMode
        orderEntry.order >> order

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == deliveryMode
    }

    @Test
    def 'Should return delivery mode from order and point of service as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def pointOfService = Mock(PointOfServiceModel)
        orderEntry.deliveryPointOfService >> pointOfService

        and:
        def order = Mock(OrderModel)
        orderEntry.order >> order
        def deliveryMode = Stub(DeliveryModeModel)

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        1 * pickupDeliveryModeDao.findPickupDeliveryModesForAbstractOrder(order) >> [deliveryMode]
        groupingObject == deliveryMode
    }

    @Test
    def 'Should set grouping delivery mode as delivery mode for the consignment created'()
    {
        given:
        def consignment = Mock(ConsignmentModel)
        def deliveryMode = Stub(DeliveryModeModel)

        when:
        strategy.afterSplitting(deliveryMode, consignment)

        then:
        1 * consignment.setDeliveryMode(deliveryMode)
    }
}
