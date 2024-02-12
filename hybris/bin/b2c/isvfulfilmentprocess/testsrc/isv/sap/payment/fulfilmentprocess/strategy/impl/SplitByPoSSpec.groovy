package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.storelocator.model.PointOfServiceModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SplitByPoSSpec extends Specification
{
    def strategy = new SplitByPoS()

    @Test
    def 'Should return delivery point of service as grouping object'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def pointOfService = Stub(PointOfServiceModel)
        orderEntry.deliveryPointOfService >> pointOfService

        when:
        def groupingObject = strategy.getGroupingObject(orderEntry)

        then:
        groupingObject == pointOfService
    }

    @Test
    def 'Should set delivery point of service to created consignment'()
    {
        given:
        def pointOfService = Stub(PointOfServiceModel)
        def consignment = Mock(ConsignmentModel)

        when:
        strategy.afterSplitting(pointOfService, consignment)

        then:
        1 * consignment.setDeliveryPointOfService(pointOfService)
    }
}
