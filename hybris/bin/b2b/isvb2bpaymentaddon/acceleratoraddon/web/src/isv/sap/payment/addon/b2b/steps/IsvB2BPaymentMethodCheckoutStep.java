package isv.sap.payment.addon.b2b.steps;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;

public class IsvB2BPaymentMethodCheckoutStep extends CheckoutStep
{
    private CheckoutFacade checkoutFacade;

    @Override
    public boolean isEnabled()
    {
        final CartData checkoutCart = getCheckoutFacade().getCheckoutCart();
        if (checkoutCart == null || checkoutCart.getPaymentType() == null)
        {
            return false;
        }
        return !CheckoutPaymentType.ACCOUNT.getCode().equals(checkoutCart.getPaymentType().getCode());
    }

    protected CheckoutFacade getCheckoutFacade()
    {
        return checkoutFacade;
    }

    public void setCheckoutFacade(final CheckoutFacade checkoutFacade)
    {
        this.checkoutFacade = checkoutFacade;
    }
}
