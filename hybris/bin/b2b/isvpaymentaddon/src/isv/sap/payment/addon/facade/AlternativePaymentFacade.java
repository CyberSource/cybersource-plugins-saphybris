package isv.sap.payment.addon.facade;

import java.util.Map;
import java.util.Optional;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to Alternative payments.
 */
public interface AlternativePaymentFacade
{
    /**
     * Makes sale (or initiate for Alipay) request for an alternative payment
     *
     * @param cart current cart
     * @param paymentModeCode code of payment method selected (must be one of PaymentType.ALTERNATIVE_PAYMENT)
     * @param optionalParameters optional parameters specific for concrete type of alternative payments
     *
     * @return redirect URL in case of success call
     */
    Optional<String> makeSaleRequestForAlternativePayment(final CartModel cart, final String paymentModeCode,
            final Map<String, Object> optionalParameters);

    /**
     * Validates if a response from alternative payment is valid.
     * This is reference implementation, for production more validation logic can/need to be added.
     *
     * @param cart AbstractOrder containing the transaction to validate
     * @param alternativePaymentType expected payment type
     * @return true if transaction is valid (i.e. the same type, amounts are the same, etc.)
     */
    boolean validateAlternativePaymentResponse(final AbstractOrderModel cart, final String alternativePaymentType);
}
