package isv.sap.payment.fulfilmentprocess.actions.returns;

import java.util.Set;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation for approving the ReturnRequest
 */
public class ApproveReturnAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(ApproveReturnAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }
}
