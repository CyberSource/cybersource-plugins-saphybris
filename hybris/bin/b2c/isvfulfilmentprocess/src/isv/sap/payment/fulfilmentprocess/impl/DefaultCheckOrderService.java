package isv.sap.payment.fulfilmentprocess.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import isv.sap.payment.fulfilmentprocess.CheckOrderService;

/**
 * Default implementation of {@link CheckOrderService}
 */
public class DefaultCheckOrderService implements CheckOrderService
{
    @Override
    public boolean check(final OrderModel order)
    {
        if (!order.getCalculated().booleanValue())
        {
            return false;
        }
        if (order.getEntries().isEmpty())
        {
            return false;
        }
        else if (order.getPaymentInfo() == null)
        {
            return false;
        }
        else
        {
            return checkDeliveryOptions(order);
        }
    }

    protected boolean checkDeliveryOptions(final OrderModel order)
    {
        if (order.getDeliveryMode() == null)
        {
            return false;
        }

        if (order.getDeliveryAddress() == null)
        {
            for (final AbstractOrderEntryModel entry : order.getEntries())
            {
                if (entry.getDeliveryPointOfService() == null && entry.getDeliveryAddress() == null)
                {
                    return false;
                }
            }
        }

        return true;
    }
}
