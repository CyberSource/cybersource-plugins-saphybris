package isv.sap.payment.addon.facade;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to PayPal payment.
 */
public interface PayPalPaymentFacade
{
    /**
     * Authorizes paypal payment for given cart.
     * paypal authorization is based on results of paypal SET command and includes 3 consecutive operations:
     * paypal GET
     * paypal OrderSetup
     * paypal Authorize
     *
     * @param cart cart
     * @param ecToken Express Checout token which is returned from SET operation
     * @return true is authorization was successful
     */
    boolean authorizePayPalPayment(final CartModel cart, final String ecToken);

    /**
     * Executes paypal Set request.
     *
     * @param cart the shopping cart object
     * @return the paypal transaction TOKEN
     */
    String executePayPalExpressCheckoutCreateSessionRequest(final CartModel cart);
}
