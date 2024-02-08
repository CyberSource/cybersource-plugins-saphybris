package isv.sap.payment.fulfilmentprocess.listeners

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.events.PickupConfirmationEvent
import de.hybris.platform.ordersplitting.model.ConsignmentModel
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.processengine.BusinessProcessService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants

@UnitTest
class PickupConfirmationEventListenerSpec extends Specification
{
    def businessProcessService = Mock(BusinessProcessService)

    def listener = new PickupConfirmationEventListener(
            businessProcessService: businessProcessService
    )

    @Test
    def 'Should trigger pickup event for consignment'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)
        consignmentProcess.code >> 'process12345'
        def consignment = Mock(ConsignmentModel)
        consignment.consignmentProcesses >> [consignmentProcess]
        consignmentProcess.consignment >> consignment
        def event = new PickupConfirmationEvent(consignmentProcess)

        when:
        listener.onEvent(event)

        then:
        1 * businessProcessService.triggerEvent(_ as String) >> { args ->
            def eventName = args[0] as String
            assert eventName == "${consignmentProcess.code}_${IsvfulfilmentprocessConstants.CONSIGNMENT_PICKUP}"
        }
    }
}
