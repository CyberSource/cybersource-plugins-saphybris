package isv.sap.payment.fulfilmentprocess.warehouse

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.enums.WarehouseConsignmentState
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.processengine.BusinessProcessService
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.warehouse.WarehouseConsignmentStatus
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants

@UnitTest
class DefaultWarehouse2ProcessAdapterSpec extends Specification
{
    def statusMap = [(WarehouseConsignmentStatus.COMPLETE): WarehouseConsignmentState.COMPLETE]

    def modelService = Mock(ModelService)

    def businessProcessService = Mock(BusinessProcessService)

    def adapter = new DefaultWarehouse2ProcessAdapter(
            statusMap: statusMap,
            modelService: modelService,
            businessProcessService: businessProcessService
    )

    @Test
    def 'Should set warehouse consignment status to consignment process'()
    {
        given:
        def consignment = Mock(ConsignmentModel)
        def consignmentProcess = Mock(ConsignmentProcessModel)
        consignmentProcess.code >> 'test12345'
        consignment.consignmentProcesses >> [consignmentProcess]

        when:
        adapter.receiveConsignmentStatus(consignment, WarehouseConsignmentStatus.COMPLETE)

        then:
        1 * consignmentProcess.setWarehouseConsignmentState(WarehouseConsignmentState.COMPLETE)
        1 * modelService.save(consignmentProcess)
        1 * businessProcessService.triggerEvent("${consignmentProcess.getCode()}_${IsvfulfilmentprocessConstants.WAIT_FOR_WAREHOUSE}")
    }

    @Test
    def 'Should throw exception when no mapping for warehouse consignment status is found'()
    {
        given:
        def consignment = Mock(ConsignmentModel)
        def consignmentProcess = Mock(ConsignmentProcessModel)
        consignmentProcess.code >> 'test12345'
        consignment.consignmentProcesses >> [consignmentProcess]

        when:
        adapter.receiveConsignmentStatus(consignment, WarehouseConsignmentStatus.CANCEL)

        then:
        def exception = thrown(IllegalStateException)
        exception.message == "No mapping for WarehouseConsignmentStatus: ${WarehouseConsignmentStatus.CANCEL}"
        0 * modelService.save(_)
    }
}
