package isv.sap.payment.addon.b2b.fulfilmentprocess.actions;

import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

import isv.sap.payment.fulfilmentprocess.actions.order.CheckAuthorizeOrderPaymentAction;

public class B2BCheckAuthorizeOrderPaymentAction extends CheckAuthorizeOrderPaymentAction
{
    @Override
    protected Transition assignStatusForOrder(final OrderModel order)
    {
        if (CheckoutPaymentType.ACCOUNT.equals(order.getPaymentType()))
        {
            setOrderStatus(order, OrderStatus.PAYMENT_AUTHORIZED);
            return Transition.OK;
        }
        return super.assignStatusForOrder(order);
    }
}
