package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.storelocator.model.PointOfServiceModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SplitByEntryDeliveryAddressSpec extends Specification
{
    def strategy = new SplitByEntryDeliveryAddress()

    @Test
    def 'Should return delivery address from entry as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def deliveryAddress = Stub(AddressModel)
        orderEntry.deliveryAddress >> deliveryAddress

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == deliveryAddress
    }

    @Test
    def 'Should return address from point of service as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def address = Stub(AddressModel)
        def pointOfService = Mock(PointOfServiceModel)
        pointOfService.address >> address
        orderEntry.deliveryPointOfService >> pointOfService

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == address
    }

    @Test
    def 'Should return delivery address from order as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def pointOfService = Mock(PointOfServiceModel)
        orderEntry.deliveryPointOfService >> pointOfService

        and:
        def order = Mock(OrderModel)
        def deliveryAddress = Stub(AddressModel)
        order.deliveryAddress >> deliveryAddress
        orderEntry.order >> order

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == deliveryAddress
    }

    @Test
    def 'Should set grouping address as shipping address for the consignment created'()
    {
        given:
        def consignment = Mock(ConsignmentModel)
        def deliveryAddress = Stub(AddressModel)

        when:
        strategy.afterSplitting(deliveryAddress, consignment)

        then:
        1 * consignment.setShippingAddress(deliveryAddress)
    }
}
