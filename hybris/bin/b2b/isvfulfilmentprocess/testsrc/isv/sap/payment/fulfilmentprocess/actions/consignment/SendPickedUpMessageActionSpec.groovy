package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.events.SendPickedUpMessageEvent
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.event.EventService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SendPickedUpMessageActionSpec extends Specification
{
    def eventService = Mock(EventService)

    def action = new SendPickedUpMessageAction(eventService: eventService)

    @Test
    def 'Should publish picked up message event'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        action.execute(consignmentProcess)

        then:
        1 * eventService.publishEvent(_) >> { args ->
            def event = args[0] as SendPickedUpMessageEvent
            assert event.process == consignmentProcess
        }
    }
}
