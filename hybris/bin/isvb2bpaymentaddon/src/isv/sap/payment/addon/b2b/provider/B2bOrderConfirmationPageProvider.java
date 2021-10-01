package isv.sap.payment.addon.b2b.provider;

import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

import isv.sap.payment.addon.provider.DefaultOrderConfirmationPageProvider;

public class B2bOrderConfirmationPageProvider extends DefaultOrderConfirmationPageProvider
{
    @Override
    public String getOrderConfirmationPage(final AbstractOrderData orderData)
    {
        if (orderData instanceof ScheduledCartData)
        {
            return "/checkout/replenishment/confirmation/" + ((ScheduledCartData) orderData).getJobCode();
        }

        return super.getOrderConfirmationPage(orderData);
    }
}
