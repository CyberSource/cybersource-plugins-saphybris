package isv.sap.payment.addon.facade;

import java.util.Map;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to GooglePay payment.
 */
public interface GooglePayPaymentFacade
{
    /**
     * Authorizes Google Pay payment for a given paymentData and cart
     *
     * @param paymentData Map representing Google Pay paymentData https://developers.google.com/pay/api/web/reference/response-objects#PaymentData
     * @param cart cart originating the payment
     * @return true if authorization succeeded, false otherwise
     */
    boolean authorizeGooglePayPayment(final Map<String, Object> paymentData, final CartModel cart);
}
