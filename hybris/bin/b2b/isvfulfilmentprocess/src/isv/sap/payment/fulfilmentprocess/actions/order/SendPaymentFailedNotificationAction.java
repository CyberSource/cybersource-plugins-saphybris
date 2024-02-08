package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.orderprocessing.events.PaymentFailedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class SendPaymentFailedNotificationAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendPaymentFailedNotificationAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final OrderProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
        getEventService().publishEvent(new PaymentFailedEvent(process));
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
