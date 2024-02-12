package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.orderprocessing.events.OrderPlacedEvent
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.servicelayer.event.EventService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class SendOrderPlacedNotificationActionSpec extends Specification
{
    def eventService = Mock(EventService)

    def action = new SendOrderPlacedNotificationAction(
            eventService: eventService
    )

    @Test
    def 'Should publish event for process'()
    {
        given:
        def process = Mock(OrderProcessModel)

        when:
        action.executeAction(process)

        then:
        1 * eventService.publishEvent(_ as OrderPlacedEvent) >> { args ->
            def event = args[0] as OrderPlacedEvent
            assert event.process == process
        }
    }
}
