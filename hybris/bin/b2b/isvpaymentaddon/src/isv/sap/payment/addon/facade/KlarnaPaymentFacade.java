package isv.sap.payment.addon.facade;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to Klarna payment.
 */
public interface KlarnaPaymentFacade
{
    /**
     * Creates Klarna session and returns its session id.
     *
     * @param sessionCart current cart
     * @return id of created Klarna session
     */
    String createKlarnaSession(final CartModel sessionCart);

    /**
     * Creates Klarna session and returns its session id.
     *
     * @param sessionCart current cart
     * @return id of created Klarna session
     */
    String updateKlarnaSession(final CartModel sessionCart);
}
