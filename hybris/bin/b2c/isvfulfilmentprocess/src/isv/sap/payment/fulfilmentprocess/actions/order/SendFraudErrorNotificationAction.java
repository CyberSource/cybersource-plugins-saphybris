package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.orderprocessing.events.FraudErrorEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendFraudErrorNotificationAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendFraudErrorNotificationAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final OrderProcessModel process)
    {
        eventService.publishEvent(new FraudErrorEvent(process));
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }

    protected EventService getEventService()
    {
        return eventService;
    }

    
    public void setEventService(final EventService eventService)
    {
        this.eventService = eventService;
    }
}
