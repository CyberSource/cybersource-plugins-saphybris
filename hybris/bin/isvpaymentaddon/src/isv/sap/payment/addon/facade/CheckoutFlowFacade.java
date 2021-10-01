package isv.sap.payment.addon.facade;

import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of {@link SessionOverrideCheckoutFlowFacade} component.
 * <p>
 * This implementation is used to redefine session checkout flow logic.
 */
public class CheckoutFlowFacade extends SessionOverrideCheckoutFlowFacade
{
    /**
     * Detects missing payment info on session cart object.
     *
     * @return true if there is no payment info on cart.
     */
    @Override
    public boolean hasNoPaymentInfo()
    {
        final CartModel cart = getCart();

        return cart.getPaymentInfo() == null;
    }
}
