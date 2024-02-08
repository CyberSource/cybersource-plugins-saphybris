package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.enums.WarehouseConsignmentState
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class ReceiveConsignmentStatusActionSpec extends Specification
{
    def modelService = Mock(ModelService)
    def action = new ReceiveConsignmentStatusAction(modelService: modelService)

    @Test
    @Unroll
    def 'Should determine the transition based on the warehouse consignment state'()
    {
        given:
        def process = Mock(ConsignmentProcessModel)
        process.warehouseConsignmentState >> consignmentStateParam

        when:
        def transition = action.execute(process)

        then:
        1 * process.setWaitingForConsignment(false)
        1 * modelService.save(process)

        and:
        transition == expectedTransition

        where:
        consignmentStateParam              | expectedTransition
        WarehouseConsignmentState.CANCEL   | 'CANCEL'
        WarehouseConsignmentState.COMPLETE | 'OK'
        WarehouseConsignmentState.PARTIAL  | 'ERROR'
    }

    @Test
    def 'Should return transition to error when warehouse consignment state is null'()
    {
        given:
        def process = Mock(ConsignmentProcessModel)

        when:
        def transition = action.execute(process)

        then:
        1 * process.setWaitingForConsignment(false)
        1 * modelService.save(process)

        and:
        transition == 'ERROR'
    }
}
