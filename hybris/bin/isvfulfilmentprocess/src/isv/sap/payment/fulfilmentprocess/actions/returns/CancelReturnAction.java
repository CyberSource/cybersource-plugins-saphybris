package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation for cancelling the ReturnRequest
 */
public class CancelReturnAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CancelReturnAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());

        final ReturnRequestModel returnRequest = process.getReturnRequest();
        returnRequest.setStatus(ReturnStatus.CANCELED);
        returnRequest.getReturnEntries().forEach(entry -> {
            entry.setStatus(ReturnStatus.CANCELED);
            getModelService().save(entry);
        });
        getModelService().save(returnRequest);
    }
}
