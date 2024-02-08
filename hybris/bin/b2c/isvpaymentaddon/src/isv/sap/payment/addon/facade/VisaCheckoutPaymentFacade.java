package isv.sap.payment.addon.facade;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to VisaCheckout payment.
 */
public interface VisaCheckoutPaymentFacade
{
    /**
     * Authorizes visa checkout payment for given cart.
     *
     * @param sessionCart current cart
     * @param callId visa checkout call id, it should be used as orderId
     * @return false if visa checkout Authorization service fails. Otherwise returns true
     */
    boolean authorizeVisaCheckoutPayment(final CartModel sessionCart, final String callId);

    /**
     * Authorizes visa checkout payment for given cart.
     *
     * @param sessionCart current cart
     * @param callId visa checkout call id, it should be used as orderId
     * @param performGetFirst should perform a get request first
     * @return false if visa checkout Authorization service fails. Otherwise returns true
     */
    boolean authorizeVisaCheckoutPayment(final CartModel sessionCart, final String callId,
            final boolean performGetFirst);

    /**
     * Gets visa checkout payment data for given cart.
     * Updates the cart info with Get visa checkout data.
     *
     * @param sessionCart current cart
     * @param callId visa checkout call id, it should be used as orderId
     * @return false if visa checkout Get. Otherwise returns true
     */
    boolean updateCartAddressesWithVCGetData(final CartModel sessionCart, final String callId);
}
