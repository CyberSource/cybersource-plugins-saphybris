package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.processengine.BusinessProcessService
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_END_EVENT_NAME

@UnitTest
class SubprocessEndActionSpec extends Specification
{
    def businessProcessService = Mock(BusinessProcessService)
    def modelService = Mock(ModelService)

    def action = new SubprocessEndAction(businessProcessService: businessProcessService, modelService: modelService)

    @Test
    def 'Should end subprocess and trigger event for parent process'()
    {
        given:
        def orderProcess = Stub(OrderProcessModel)
        orderProcess.code >> 'orderProcess12345'
        def consignmentProcess = Mock(ConsignmentProcessModel)
        consignmentProcess.parentProcess >> orderProcess

        when:
        action.execute(consignmentProcess)

        then:
        1 * consignmentProcess.setDone(true)
        1 * modelService.save(consignmentProcess)
        1 * businessProcessService.triggerEvent("${orderProcess.code}_${CONSIGNMENT_SUBPROCESS_END_EVENT_NAME}")
    }
}
