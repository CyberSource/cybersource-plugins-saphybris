package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.warehouse.Process2WarehouseAdapter
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SendConsignmentToWarehouseActionSpec extends Specification
{
    def process2WarehouseAdapter = Mock(Process2WarehouseAdapter)

    def modelService = Mock(ModelService)

    def action = new SendConsignmentToWarehouseAction(
            process2WarehouseAdapter: process2WarehouseAdapter,
            modelService: modelService
    )

    @Test
    def 'Should send consignment to warehouse'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def consignment = Stub(ConsignmentModel)
        consignmentProcess.consignment >> consignment

        when:
        action.execute(consignmentProcess)

        then:
        1 * process2WarehouseAdapter.prepareConsignment(consignment)
        1 * consignmentProcess.setWaitingForConsignment(true)
        1 * modelService.save(consignmentProcess)
    }
}
