package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

public class WaitBeforeTransmissionAction extends AbstractSimpleDecisionAction<ConsignmentProcessModel>
{
    @Override
    public Transition executeAction(final ConsignmentProcessModel process)
    {
        return Transition.OK;
    }
}
