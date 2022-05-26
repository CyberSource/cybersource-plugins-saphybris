package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Map;
import java.util.Set;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class CheckAlternativePaymentAction extends AbstractAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CheckAlternativePaymentAction.class);

    private Map<OrderStatus, Transition> transitionMap;

    @Override
    public String execute(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();
        final Transition transition = transitionMap.get(order.getStatus());

        if (transition != null)
        {
            LOG.info("The alternative payment status on order[{}] is {}", order.getGuid(), transition);
            return transition.toString();
        }
        throw new IllegalStateException(format("Could not find transition for order status [%s]", order.getStatus()));
    }

    @Override
    public Set<String> getTransitions()
    {
        return stream(Transition.values()).map(Transition::toString).collect(toSet());
    }

    @Required
    public void setTransitionMap(final Map<OrderStatus, Transition> transitionMap)
    {
        this.transitionMap = transitionMap;
    }

    protected enum Transition
    {
        OK, NOK, WAIT, PAY
    }
}
