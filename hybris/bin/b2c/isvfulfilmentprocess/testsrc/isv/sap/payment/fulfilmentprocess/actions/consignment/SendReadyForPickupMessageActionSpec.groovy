package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.events.SendReadyForPickupMessageEvent
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import de.hybris.platform.servicelayer.event.EventService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SendReadyForPickupMessageActionSpec extends Specification
{
    def eventService = Mock(EventService)

    def action = new SendReadyForPickupMessageAction(eventService: eventService)

    @Test
    def 'Should publish ready for pickup message event'()
    {
        given:
        def consignmentProcess = Mock(ConsignmentProcessModel)

        when:
        action.execute(consignmentProcess)

        then:
        1 * eventService.publishEvent(_) >> { args ->
            def event = args[0] as SendReadyForPickupMessageEvent
            assert event.process == consignmentProcess
        }
    }
}
