package isv.sap.payment.commercefacades.order;

import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.InvalidCartException;

/**
 * Checkout facade interface to provide methods to work with cart models during payment process.
 */
public interface PaymentCheckoutFacade
{
    /**
     * Places order from specified cart.
     *
     * @param cart the cart to be used for order placement
     * @return order data dto
     * @throws InvalidCartException if order cannot be placed
     */
    AbstractOrderData performPlaceOrder(final CartModel cart) throws InvalidCartException;

    /**
     * Checks the validity of card data (e.g. delivery address, delivery mode, payment info, etc.).
     *
     * @param cart the cart to be validated
     * @return true if cart data is valid, otherwise false
     */
    boolean validOrder(final CartModel cart);
}
