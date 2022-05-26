package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation for accepting goods for the ReturnRequest
 */
public class AcceptGoodsAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(AcceptGoodsAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
    }
}
