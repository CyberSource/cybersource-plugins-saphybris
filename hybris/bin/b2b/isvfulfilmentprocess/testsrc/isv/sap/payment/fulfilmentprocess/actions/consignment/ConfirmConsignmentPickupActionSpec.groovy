package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ConsignmentStatus
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class ConfirmConsignmentPickupActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new ConfirmConsignmentPickupAction(modelService: modelService)

    @Test
    def 'Should mark consignment status as pickup complete'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def consignment = Mock(ConsignmentModel)
        consignmentProcess.consignment >> consignment

        when:
        def transition = action.execute(consignmentProcess)

        then:
        1 * consignment.setStatus(ConsignmentStatus.PICKUP_COMPLETE)
        1 * modelService.save(consignment)
        transition == 'OK'
    }

    @Test
    def 'Should transition to error when consignment is null'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        def transition = action.execute(consignmentProcess)

        then:
        transition == 'ERROR'
    }
}
