package isv.sap.payment.fulfilmentprocess.actions.consignment;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveConsignmentStatusAction extends AbstractAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(ReceiveConsignmentStatusAction.class);

    @Override
    public String execute(final ConsignmentProcessModel process)
    {
        Transition result;
        if (process.getWarehouseConsignmentState() == null)
        {
            LOG.error("Process has no warehouse consignment state");
            result = Transition.ERROR;
        }
        else
        {
            switch (process.getWarehouseConsignmentState()) // NOPMD
            {
                case CANCEL:
                    result = Transition.CANCEL;
                    break;
                case COMPLETE:
                    result = Transition.OK;
                    break;
                default:
                    LOG.error("Unexpected warehouse consignment state: {}", process.getWarehouseConsignmentState());
                    result = Transition.ERROR;
            }
        }
        process.setWaitingForConsignment(false);
        getModelService().save(process);
        return result.toString();
    }

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }

    public enum Transition
    {
        OK, CANCEL, ERROR;

        public static Set<String> getStringValues()
        {
            final Set<String> res = new HashSet<>();

            for (final Transition transition : Transition.values())
            {
                res.add(transition.toString());
            }
            return res;
        }
    }
}
