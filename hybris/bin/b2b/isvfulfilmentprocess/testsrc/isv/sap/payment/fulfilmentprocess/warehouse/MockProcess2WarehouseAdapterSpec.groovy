package isv.sap.payment.fulfilmentprocess.warehouse

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ConsignmentStatus
import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel
import de.hybris.platform.core.PK
import de.hybris.platform.core.model.order.OrderEntryModel
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.time.TimeService
import de.hybris.platform.warehouse.Warehouse2ProcessAdapter
import de.hybris.platform.warehouse.WarehouseConsignmentStatus
import org.junit.Test
import spock.lang.Specification

@UnitTest
class MockProcess2WarehouseAdapterSpec extends Specification
{
    def modelService = Mock(ModelService)

    def warehouse2ProcessAdapter = Mock(Warehouse2ProcessAdapter)

    def timeService = Mock(TimeService)

    def adapter = new MockProcess2WarehouseAdapter(
            modelService: modelService,
            warehouse2ProcessAdapter: warehouse2ProcessAdapter,
            timeService: timeService
    )

    @Test
    def 'Should prepare consignment'()
    {
        def consignmentEntry = Mock(ConsignmentEntryModel)
        consignmentEntry.quantity >> 5
        def consignment = Mock(ConsignmentModel)
        consignment.consignmentEntries >> [consignmentEntry]
        consignment.pk >> PK.fromLong(12345)

        when:
        adapter.prepareConsignment(consignment)

        then:
        1 * consignmentEntry.setShippedQuantity(5)
        1 * consignment.setStatus(ConsignmentStatus.READY)
        1 * modelService.save(consignment)
        1 * modelService.get(_ as PK) >> consignment
        1 * warehouse2ProcessAdapter.receiveConsignmentStatus(consignment, WarehouseConsignmentStatus.COMPLETE)
    }

    @Test
    def 'Should set consignment as shipped'()
    {
        def consignmentEntry = Mock(ConsignmentEntryModel)
        def orderEntry = Mock(OrderEntryModel)
        orderEntry.quantity >> 5
        consignmentEntry.orderEntry >> orderEntry
        def consignment = Mock(ConsignmentModel)
        consignment.deliveryMode >> deliveryModeParam
        consignment.consignmentEntries >> [consignmentEntry]

        when:
        adapter.shipConsignment(consignment)

        then:
        1 * consignmentEntry.setShippedQuantity(5)
        1 * modelService.save(consignmentEntry)

        and:
        1 * consignment.setStatus(consignmentStatusParam)
        1 * modelService.save(consignment)

        where:
        deliveryModeParam             | consignmentStatusParam
        Stub(PickUpDeliveryModeModel) | ConsignmentStatus.READY_FOR_PICKUP
        Stub(ZoneDeliveryModeModel)   | ConsignmentStatus.SHIPPED
    }

    @Test
    def 'Should do nothing when consignment is null'()
    {
        when:
        adapter.shipConsignment(null)

        then:
        noExceptionThrown()
    }
}
