package isv.sap.payment.addon.provider;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;

public interface OrderConfirmationPageProvider
{
    String getOrderConfirmationPage(final AbstractOrderData orderData);
}
