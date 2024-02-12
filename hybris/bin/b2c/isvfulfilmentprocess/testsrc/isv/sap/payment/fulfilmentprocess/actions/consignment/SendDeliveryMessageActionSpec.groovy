package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.events.SendDeliveryMessageEvent
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.event.EventService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SendDeliveryMessageActionSpec extends Specification
{
    def eventService = Mock(EventService)

    def action = new SendDeliveryMessageAction(eventService: eventService)

    @Test
    def 'Should publish delivery message event'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        action.execute(consignmentProcess)

        then:
        1 * eventService.publishEvent(_) >> { args ->
            def event = args[0] as SendDeliveryMessageEvent
            assert event.process == consignmentProcess
        }
    }
}
