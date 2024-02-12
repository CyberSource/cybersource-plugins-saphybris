package isv.sap.payment.fulfilmentprocess.actions.consignment;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmConsignmentPickupAction extends AbstractAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmConsignmentPickupAction.class);

    @Override
    public String execute(final ConsignmentProcessModel process)
    {
        final ConsignmentModel consignment = process.getConsignment();
        if (consignment != null)
        {
            consignment.setStatus(ConsignmentStatus.PICKUP_COMPLETE);
            getModelService().save(consignment);
            return Transition.OK.toString();
        }
        LOG.error("Process has no consignment");
        return Transition.ERROR.toString();
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
