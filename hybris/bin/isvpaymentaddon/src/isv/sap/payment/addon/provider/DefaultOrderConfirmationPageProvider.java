package isv.sap.payment.addon.provider;

import javax.annotation.Resource;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;

public class DefaultOrderConfirmationPageProvider implements OrderConfirmationPageProvider
{
    @Resource(name = "checkoutCustomerStrategy")
    private CheckoutCustomerStrategy checkoutCustomerStrategy;

    @Override
    public String getOrderConfirmationPage(final AbstractOrderData orderData)
    {
        final boolean anonymous = checkoutCustomerStrategy.isAnonymousCheckout();
        return "/checkout/orderConfirmation/" + (anonymous ? orderData.getGuid() : orderData.getCode());
    }

    public void setCheckoutCustomerStrategy(final CheckoutCustomerStrategy checkoutCustomerStrategy)
    {
        this.checkoutCustomerStrategy = checkoutCustomerStrategy;
    }
}
