package isv.sap.payment.fulfilmentprocess.test.actions;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubprocessesCompleted extends TestActionTemp<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SubprocessesCompleted.class);

    @Override
    public String execute(final OrderProcessModel process) throws Exception //NOPMD
    {
        for (final ConsignmentProcessModel subProcess : process.getConsignmentProcesses())
        {
            modelService.refresh(subProcess);
            if (!subProcess.isDone())
            {

                LOG.info("Process: {} found  subprocess {} incomplete -> wait again!", process.getCode(),
                        subProcess.getCode());
                return "NOK";
            }
        }
        return "OK";
    }
}
