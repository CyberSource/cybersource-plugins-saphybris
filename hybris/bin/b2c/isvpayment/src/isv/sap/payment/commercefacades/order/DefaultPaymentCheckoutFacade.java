package isv.sap.payment.commercefacades.order;

import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;

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

    @Resource
    private CartService cartService;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPaymentCheckoutFacade.class);
 
    @Override
    public boolean validateCart(){
        final CartModel sessionCart = cartService.getSessionCart();
        final Double authorizedTotal = sessionCart.getTotalPrice();
        final int authorizedItemCount = sessionCart.getEntries().size();
        final String cartCode = sessionCart.getCode();
 
        final CartModel currentCart = cartService.getSessionCart();
 
        // Validate cart integrity: verify the cart hasn't been modified between authorization and placement
        if (!cartCode.equals(currentCart.getCode()))
        {
            LOG.error("Cart code mismatch detected. Expected: {}, Current: {}. Possible session manipulation.",
                    cartCode, currentCart.getCode());
            return false;
        }
 
        final Double currentTotal = currentCart.getTotalPrice();
        final int currentItemCount = currentCart.getEntries().size();
 
        if (!authorizedTotal.equals(currentTotal) || authorizedItemCount != currentItemCount)
        {
            LOG.error("Cart modification detected between authorization and placement. " +
                    "Authorized total: {}, Current total: {}. " +
                    "Authorized items: {}, Current items: {}. " +
                    "Rejecting order to prevent race condition exploit.",
                    authorizedTotal, currentTotal, authorizedItemCount, currentItemCount);
            return false;
        }
        return true;
    }
}


 
    
 