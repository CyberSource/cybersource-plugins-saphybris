package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation to execute when tax was reversed successfully.
 */
public class SuccessTaxReverseAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SuccessTaxReverseAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }
}
