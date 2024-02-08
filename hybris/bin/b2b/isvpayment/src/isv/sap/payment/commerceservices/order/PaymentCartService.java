package isv.sap.payment.commerceservices.order;

import de.hybris.platform.core.model.order.CartModel;

/**
 * Service provides payment related methods to work with cart model.
 */
public interface PaymentCartService
{
    /**
     * Locate a cart for the specified cart guid.
     *
     * @param guid the guid of the cart
     * @return the specified cart
     */
    CartModel getCartForGuid(String guid);

    /**
     * Obtains the lock on given cart model and executes specified body in transaction.
     *
     * @param cart the cart to be locked
     * @param body the logic to be executed
     */
    void executeWithCartLock(final CartModel cart, final Runnable body);
}
