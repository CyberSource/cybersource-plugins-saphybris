package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderEntryModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.WarehouseModel
import de.hybris.platform.ordersplitting.strategy.impl.OrderEntryGroup
import de.hybris.platform.store.BaseStoreModel
import de.hybris.platform.storelocator.model.PointOfServiceModel
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class SplitByWarehouseSpec extends Specification
{
    def strategy = new SplitByWarehouse()

    @Test
    def 'Should group entry for available warehouses from the store'()
    {
        given:
        def warehouse1 = Stub(WarehouseModel)
        def warehouse2 = Stub(WarehouseModel)
        def warehouse3 = Stub(WarehouseModel)
        def baseStore = Mock(BaseStoreModel)
        baseStore.warehouses >> [warehouse1, warehouse2, warehouse3]

        def order = Mock(OrderModel)
        order.store >> baseStore

        def orderEntry = Mock(OrderEntryModel)
        orderEntry.order >> order

        def orderEntryGroup = new OrderEntryGroup()
        orderEntryGroup.add(orderEntry)

        when:
        def result = strategy.perform([orderEntryGroup])

        then:
        result.size() == 1
        result[0].contains(orderEntry)
        result[0].getParameter('WAREHOUSE_LIST') == [warehouse1, warehouse2, warehouse3]
    }

    @Test
    def 'Should group entry for available warehouses from point of service'()
    {
        given:
        def order = Mock(OrderModel)
        def store = Mock(BaseStoreModel)
        order.store >> store
        def warehouse1 = Stub(WarehouseModel)
        def warehouse2 = Stub(WarehouseModel)
        def warehouse3 = Stub(WarehouseModel)

        and:
        def pointOfService1 = Mock(PointOfServiceModel)
        pointOfService1.warehouses >> [warehouse1, warehouse2]
        def orderEntry1 = Mock(OrderEntryModel)
        orderEntry1.order >> order
        orderEntry1.deliveryPointOfService >> pointOfService1

        and:
        def pointOfService2 = Mock(PointOfServiceModel)
        pointOfService2.warehouses >> [warehouse3]
        def orderEntry2 = Mock(OrderEntryModel)
        orderEntry2.order >> order
        orderEntry2.deliveryPointOfService >> pointOfService2

        and:
        def orderEntry3 = Mock(OrderEntryModel)
        orderEntry3.order >> order
        orderEntry3.deliveryPointOfService >> pointOfService2

        and:
        def orderEntryGroup = new OrderEntryGroup()
        orderEntryGroup.addAll(orderEntry1, orderEntry2, orderEntry3)

        when:
        def result = strategy.perform([orderEntryGroup])

        then:
        result.size() == 2
        result[0] == [orderEntry1]
        result[0].getParameter('WAREHOUSE_LIST') == [warehouse1, warehouse2]

        and:
        result[1] == [orderEntry2, orderEntry3]
        result[1].getParameter('WAREHOUSE_LIST') == [warehouse3]
    }

    @Test
    def 'Should set available warehouse randomly for consignment created'()
    {
        given:
        def warehouse1 = Stub(WarehouseModel)
        def warehouse2 = Stub(WarehouseModel)
        def orderEntryGroup = new OrderEntryGroup()
        orderEntryGroup.setParameter('WAREHOUSE_LIST', [warehouse1, warehouse2])
        def consignment = Mock(ConsignmentModel)

        when:
        strategy.afterSplitting(orderEntryGroup, consignment)

        then:
        1 * consignment.setWarehouse(_ as WarehouseModel) >> { args ->
            def warehouse = args[0] as WarehouseModel
            assert [warehouse1, warehouse2].contains(warehouse)
        }
    }

    @Test
    @Unroll
    def 'Should set null as available warehouse for consignment created when warehouse list is empty'()
    {
        given:
        def orderEntryGroup = new OrderEntryGroup()
        orderEntryGroup.setParameter('WAREHOUSE_LIST', warehouseListParam)
        def consignment = Mock(ConsignmentModel)

        when:
        strategy.afterSplitting(orderEntryGroup, consignment)

        then:
        1 * consignment.setWarehouse(null)

        where:
        warehouseListParam | _
        []                 | _
        null               | _
    }
}
