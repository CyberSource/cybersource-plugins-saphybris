package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation for printing the return label
 */
public class PrintReturnLabelAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(PrintReturnLabelAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }
}
