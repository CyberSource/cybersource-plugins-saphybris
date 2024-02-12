package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.orderprocessing.events.OrderCompletedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Send event representing the completion of an order process.
 */
public class SendOrderCompletedNotificationAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendOrderCompletedNotificationAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final OrderProcessModel process)
    {
        getEventService().publishEvent(new OrderCompletedEvent(process));
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }

    protected EventService getEventService()
    {
        return eventService;
    }

    @Required
    public void setEventService(final EventService eventService)
    {
        this.eventService = eventService;
    }
}
