package isv.sap.payment.addon.facade;

import java.util.Map;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to ApplePay payment.
 */
public interface ApplePayPaymentFacade
{
    /**
     * Creates ApplePay session and returns session token.
     *
     * @param validationUrl ApplePay validation URL provided on onvalidatemerchant javascript callback
     * @return ApplePay session token
     */
    Map<String, Object> createApplePaySession(final String validationUrl);

    /**
     * Authorizes ApplePay payment for a given token and cart
     *
     * @param paymentToken Map representing ApplePayPaymentToken
     * @param cart cart originating the payment
     * @return true if authorization succeeded, false otherwise
     */
    boolean authorizeApplePayPayment(final Map<String, Object> paymentToken, final CartModel cart);

    /**
     * Decrypts "data" property from ApplePayPaymentToken
     *
     * @param paymentToken Map representing ApplePayPaymentToken
     * @return Map representing a json with the decrypted contents of ApplePayPaymentToken[token][paymentData][data]
     */
    Map<String, Object> decryptApplePaymentData(final Map<String, Object> paymentToken);
}
