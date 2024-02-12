package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;

/**
 * Mark consignment as cancelled.
 */
public class CancelConsignmentAction extends AbstractProceduralAction<ConsignmentProcessModel>
{
    @Override
    public void executeAction(final ConsignmentProcessModel process)
    {
        final ConsignmentModel consignment = process.getConsignment();
        if (consignment != null)
        {
            consignment.setStatus(ConsignmentStatus.CANCELLED);
            getModelService().save(consignment);
        }
    }
}
