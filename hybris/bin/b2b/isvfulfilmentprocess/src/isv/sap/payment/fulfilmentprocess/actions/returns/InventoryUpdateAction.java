package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation for updating he inventory for the ReturnRequest
 */
public class InventoryUpdateAction extends AbstractProceduralAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(InventoryUpdateAction.class);

    @Override
    public void executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());

        final ReturnRequestModel returnRequest = process.getReturnRequest();
        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequest.getReturnEntries().forEach(entry -> {
            entry.setStatus(ReturnStatus.COMPLETED);
            getModelService().save(entry);
        });
        getModelService().save(returnRequest);
    }
}
