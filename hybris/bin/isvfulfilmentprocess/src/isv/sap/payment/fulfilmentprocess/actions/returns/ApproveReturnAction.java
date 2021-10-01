package isv.sap.payment.fulfilmentprocess.actions.returns;

import java.util.Set;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.apache.log4j.Logger;

/**
 * Mock implementation for approving the ReturnRequest
 */
public class ApproveReturnAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = Logger.getLogger(ApproveReturnAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: " + process.getCode() + " in step " + getClass().getSimpleName());
    }

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }
}
