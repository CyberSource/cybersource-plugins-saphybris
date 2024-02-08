package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.orderprocessing.events.SendPickedUpMessageEvent;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class SendPickedUpMessageAction extends AbstractProceduralAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SendPickedUpMessageAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final ConsignmentProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
        getEventService().publishEvent(getEvent(process));
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

    protected SendPickedUpMessageEvent getEvent(final ConsignmentProcessModel process)
    {
        return new SendPickedUpMessageEvent(process);
    }
}
