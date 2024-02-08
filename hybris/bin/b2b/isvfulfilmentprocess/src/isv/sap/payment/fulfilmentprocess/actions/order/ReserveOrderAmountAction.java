package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

public class ReserveOrderAmountAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        setOrderStatus(process.getOrder(), OrderStatus.PAYMENT_AMOUNT_RESERVED);
        return Transition.OK;
    }
}
