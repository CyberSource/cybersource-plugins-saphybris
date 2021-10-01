package isv.sap.payment.fulfilmentprocess.actions.consignment;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import static isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_END_EVENT_NAME;

public class SubprocessEndAction extends AbstractProceduralAction<ConsignmentProcessModel>
{
    private static final Logger LOG = Logger.getLogger(SubprocessEndAction.class);

    private static final String PROCESS_MSG = "Process: ";

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
        LOG.info(PROCESS_MSG + process.getCode() + " in step " + getClass());

        try
        {
            Thread.sleep((long) (Math.random() * 2000));
        }
        catch (final InterruptedException e)
        {
            LOG.error(e);
            Thread.currentThread().interrupt();
        }

        process.setDone(true);

        save(process);
        LOG.info(PROCESS_MSG + process.getCode() + " wrote DONE marker");

        final String processCode = process.getParentProcess().getCode();

        getBusinessProcessService().triggerEvent(processCode + "_" + CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);

        LOG.info(PROCESS_MSG + process.getCode() + " fired event " + CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);
    }
}
