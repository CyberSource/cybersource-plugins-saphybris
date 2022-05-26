package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_END_EVENT_NAME;

public class SubprocessEndAction extends AbstractProceduralAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SubprocessEndAction.class);

    private BusinessProcessService businessProcessService;

    protected BusinessProcessService getBusinessProcessService()
    {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService)
    {
        this.businessProcessService = businessProcessService;
    }

    @Override
    public void executeAction(final ConsignmentProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());

        try
        {
            Thread.sleep((long) (Math.random() * 2000));
        }
        catch (final InterruptedException e)
        {
            LOG.error("Exception", e);
            Thread.currentThread().interrupt();
        }

        process.setDone(true);

        save(process);
        LOG.info("Process: {} in step {} wrote DONE marker", process.getCode(), getClass());

        final String processCode = process.getParentProcess().getCode();

        getBusinessProcessService().triggerEvent(processCode + "_" + CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);

        LOG.info("Process: {} in step {} fired event {}", process.getCode(), getClass(),
                CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);
    }
}
