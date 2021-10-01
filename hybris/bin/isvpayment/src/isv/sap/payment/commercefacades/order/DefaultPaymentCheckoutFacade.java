package isv.sap.payment.commercefacades.order;

import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;

/**
 * Encapsulates the default implementation of {@link PaymentCheckoutFacade} interface.
 * <p>
 * Defines logic related order placement and validation.
 */
public class DefaultPaymentCheckoutFacade extends DefaultAcceleratorCheckoutFacade implements PaymentCheckoutFacade
{
    @Override
    public OrderData performPlaceOrder(final CartModel cart) throws InvalidCartException
    {
        if (cart != null)
        {
            beforePlaceOrder(cart);
            final OrderModel orderModel = placeOrder(cart);
            afterPlaceOrder(cart, orderModel);
            if (orderModel != null)
            {
                return getOrderConverter().convert(orderModel);
            }
        }

        return null;
    }

    /**
     * Defines cleanup logic called after an order is placed.
     *
     * @param cartModel source cart object
     * @param orderModel resulting order object
     */
    @Override
    protected void afterPlaceOrder(final CartModel cartModel, final OrderModel orderModel)
    {
        if (orderModel != null)
        {
            if (getCartService().hasSessionCart())
            {
                getCartService().removeSessionCart();
            }
            else
            {
                getModelService().remove(cartModel);
            }

            getModelService().refresh(orderModel);
        }
    }

    @Override
    public boolean validOrder(final CartModel cart)
    {
        return cart.getDeliveryAddress() != null && cart.getDeliveryMode() != null && cart.getPaymentInfo() != null;
    }
}
