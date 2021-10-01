package isv.sap.payment.addon.b2b.fulfilmentprocess.actions;

import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;

import isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction;

public class B2BTakePaymentAction extends TakePaymentAction
{
    @Override
    public String execute(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();

        if (CheckoutPaymentType.ACCOUNT.equals(order.getPaymentType()))
        {
            setOrderStatus(order, OrderStatus.PAYMENT_CAPTURED);
            return Transition.OK.name();
        }

        return super.execute(process);
    }
}
