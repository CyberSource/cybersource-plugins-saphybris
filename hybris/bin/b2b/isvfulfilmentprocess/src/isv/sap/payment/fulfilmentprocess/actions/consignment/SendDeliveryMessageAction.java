package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.orderprocessing.events.SendDeliveryMessageEvent;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class SendDeliveryMessageAction extends AbstractProceduralAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendDeliveryMessageAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final ConsignmentProcessModel process)
    {
        getEventService().publishEvent(getEvent(process));
        if (LOG.isInfoEnabled())
        {
            LOG.info("Process: {} in step {}", process.getCode(), getClass());
        }
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

    protected SendDeliveryMessageEvent getEvent(final ConsignmentProcessModel process)
    {
        return new SendDeliveryMessageEvent(process);
    }
}
