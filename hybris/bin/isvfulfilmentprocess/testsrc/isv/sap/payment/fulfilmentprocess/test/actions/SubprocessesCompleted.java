package isv.sap.payment.fulfilmentprocess.test.actions;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import org.apache.log4j.Logger;

public class SubprocessesCompleted extends TestActionTemp<OrderProcessModel>
{
    private static final Logger LOG = Logger.getLogger(SubprocessesCompleted.class);

    @Override
    public String execute(final OrderProcessModel process) throws Exception //NOPMD
    {
        for (final ConsignmentProcessModel subProcess : process.getConsignmentProcesses())
        {
            modelService.refresh(subProcess);
            if (!subProcess.isDone())
            {

                LOG.info("Process: " + process.getCode() + " found  subprocess " + subProcess.getCode()
                        + " incomplete -> wait again!");
                return "NOK";
            }
        }
        return "OK";
    }
}
