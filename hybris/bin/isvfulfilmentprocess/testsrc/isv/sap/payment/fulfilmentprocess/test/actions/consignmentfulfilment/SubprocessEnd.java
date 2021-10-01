package isv.sap.payment.fulfilmentprocess.test.actions.consignmentfulfilment;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.log4j.Logger;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class SubprocessEnd extends AbstractTestConsActionTemp
{
    private static final Logger LOG = Logger.getLogger(SubprocessEnd.class);

    @Override
    public String execute(final BusinessProcessModel process) throws Exception //NOPMD
    {
        super.execute(process);

        final ConsignmentProcessModel consProc = (ConsignmentProcessModel) process;
        getBusinessProcessService().triggerEvent(consProc.getParentProcess().getCode() + "_ConsignmentSubprocessEnd");
        LOG.info("Process: " + process.getCode() + " fire event "
                + IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);
        ((ConsignmentProcessModel) process).setDone(true);
        modelService.save(process);
        return getResult();
    }
}
