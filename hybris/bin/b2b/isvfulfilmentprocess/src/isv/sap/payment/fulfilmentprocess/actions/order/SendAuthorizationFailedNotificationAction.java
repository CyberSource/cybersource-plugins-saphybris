package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.orderprocessing.events.AuthorizationFailedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class SendAuthorizationFailedNotificationAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendAuthorizationFailedNotificationAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final OrderProcessModel process)
    {
        eventService.publishEvent(new AuthorizationFailedEvent(process));
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
