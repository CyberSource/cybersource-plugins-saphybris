package isv.sap.payment.fulfilmentprocess.listeners;

import de.hybris.platform.orderprocessing.events.PickupConfirmationEvent;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

/**
 * Listener for pickup confirmation events.
 */
public class PickupConfirmationEventListener extends AbstractEventListener<PickupConfirmationEvent>
{
    private BusinessProcessService businessProcessService;

    protected BusinessProcessService getBusinessProcessService()
    {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService)
    {
        this.businessProcessService = businessProcessService;
    }

    @Override
    protected void onEvent(final PickupConfirmationEvent pickupConfirmationEvent)
    {
        final ConsignmentModel consignmentModel = pickupConfirmationEvent.getProcess().getConsignment();
        for (final ConsignmentProcessModel process : consignmentModel.getConsignmentProcesses())
        {
            getBusinessProcessService().triggerEvent(
                    process.getCode() + "_" + IsvfulfilmentprocessConstants.CONSIGNMENT_PICKUP);
        }
    }
}
