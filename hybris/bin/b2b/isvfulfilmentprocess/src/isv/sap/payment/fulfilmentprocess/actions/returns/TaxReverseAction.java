package isv.sap.payment.fulfilmentprocess.actions.returns;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation to reverse tax calculation
 */
public class TaxReverseAction extends AbstractSimpleDecisionAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(TaxReverseAction.class);

    @Override
    public Transition executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());

        final ReturnRequestModel returnRequest = process.getReturnRequest();
        returnRequest.setStatus(ReturnStatus.TAX_REVERSED);
        getModelService().save(returnRequest);

        return Transition.OK;
    }
}
