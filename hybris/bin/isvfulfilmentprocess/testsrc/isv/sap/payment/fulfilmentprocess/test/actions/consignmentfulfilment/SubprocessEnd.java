package isv.sap.payment.fulfilmentprocess.test.actions.consignmentfulfilment;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class SubprocessEnd extends AbstractTestConsActionTemp
{
    private static final Logger LOG = LoggerFactory.getLogger(SubprocessEnd.class);

    @Override
    public String execute(final BusinessProcessModel process) throws Exception //NOPMD
    {
        super.execute(process);

        final ConsignmentProcessModel consProc = (ConsignmentProcessModel) process;
        getBusinessProcessService().triggerEvent(consProc.getParentProcess().getCode() + "_ConsignmentSubprocessEnd");
        LOG.info("Process: {} fire event {}", process.getCode(),
                IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_END_EVENT_NAME);
        ((ConsignmentProcessModel) process).setDone(true);
        modelService.save(process);
        return getResult();
    }
}
