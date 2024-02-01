package isv.sap.payment.commerceservices.order.dao;

import de.hybris.platform.core.model.order.CartModel;

/**
 * Provides payment DAO methods to work with cart model.
 */
public interface PaymentCartDao
{
    /**
     * Find a cart for the specified cart guid.
     *
     * @param guid the guid of the cart
     * @return the specified cart
     */
    CartModel getCartForGuid(final String guid);
}
