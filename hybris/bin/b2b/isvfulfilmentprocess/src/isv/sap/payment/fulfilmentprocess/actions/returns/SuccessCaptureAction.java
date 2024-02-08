package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation to execute when payment capture was successful.
 */
public class SuccessCaptureAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SuccessCaptureAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }
}
