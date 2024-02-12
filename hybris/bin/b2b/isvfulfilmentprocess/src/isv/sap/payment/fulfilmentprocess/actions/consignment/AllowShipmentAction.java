package isv.sap.payment.fulfilmentprocess.actions.consignment;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.warehouse.Process2WarehouseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class AllowShipmentAction extends AbstractAction<ConsignmentProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(AllowShipmentAction.class);

    private Process2WarehouseAdapter process2WarehouseAdapter;

    @Override
    public String execute(final ConsignmentProcessModel process)
    {
        final ConsignmentModel consignment = process.getConsignment();
        if (consignment != null)
        {
            try
            {
                if (OrderStatus.CANCELLED.equals(consignment.getOrder().getStatus())
                        || OrderStatus.CANCELLING.equals(consignment.getOrder().getStatus()))
                {
                    return Transition.CANCEL.toString();
                }
                else
                {
                    getProcess2WarehouseAdapter().shipConsignment(process.getConsignment());
                    return getTransitionForConsignment(consignment);
                }
            }
            catch (final Exception e)
            {
                LOG.debug("Exception", e);
                return Transition.ERROR.toString();
            }
        }
        return Transition.ERROR.toString();
    }

    protected String getTransitionForConsignment(final ConsignmentModel consignment)
    {
        if (consignment.getDeliveryMode() instanceof PickUpDeliveryModeModel)
        {
            return Transition.PICKUP.toString();
        }
        else
        {
            return Transition.DELIVERY.toString();
        }
    }

    protected Process2WarehouseAdapter getProcess2WarehouseAdapter()
    {
        return process2WarehouseAdapter;
    }

    @Required
    public void setProcess2WarehouseAdapter(final Process2WarehouseAdapter process2WarehouseAdapter)
    {
        this.process2WarehouseAdapter = process2WarehouseAdapter;
    }

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }

    public enum Transition
    {
        DELIVERY, PICKUP, CANCEL, ERROR;

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
