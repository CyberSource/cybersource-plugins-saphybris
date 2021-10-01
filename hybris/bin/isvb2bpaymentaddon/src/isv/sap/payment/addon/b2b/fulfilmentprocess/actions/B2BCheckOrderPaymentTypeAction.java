package isv.sap.payment.addon.b2b.fulfilmentprocess.actions;

import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction;

public class B2BCheckOrderPaymentTypeAction extends CheckOrderPaymentTypeAction
{
    private static final Logger LOG = LoggerFactory.getLogger(B2BCheckOrderPaymentTypeAction.class);

    @Override
    public String execute(final OrderProcessModel process) throws Exception // NOPMD
    {
        final OrderModel order = process.getOrder();

        if (order == null)
        {
            LOG.error("Missing the order, exiting the process");
            return Transition.NOK.toString();
        }

        if (CheckoutPaymentType.ACCOUNT.equals(order.getPaymentType()))
        {
            return Transition.ACCOUNT.toString();
        }

        return super.execute(process);
    }
}
