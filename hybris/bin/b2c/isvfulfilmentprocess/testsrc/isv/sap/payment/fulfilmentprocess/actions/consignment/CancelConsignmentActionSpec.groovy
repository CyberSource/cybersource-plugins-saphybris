package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ConsignmentStatus
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class CancelConsignmentActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new CancelConsignmentAction(modelService: modelService)

    @Test
    def 'Should update consignment as cancelled'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        def consignment = Mock(ConsignmentModel)
        consignmentProcess.consignment >> consignment

        when:
        action.execute(consignmentProcess)

        then:
        1 * consignment.setStatus(ConsignmentStatus.CANCELLED)
        1 * modelService.save(consignment)
    }

    @Test
    def 'Should do nothing if consignment is null'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        action.execute(consignmentProcess)

        then:
        noExceptionThrown()
    }
}
