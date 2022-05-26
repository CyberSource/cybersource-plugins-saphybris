package isv.sap.payment.addon.facade;

import java.util.Map;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.acceleratorfacades.payment.data.PaymentSubscriptionResultData;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.core.model.order.CartModel;

import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

/**
 * A customization of existing {@link PaymentFacade} which defines methods related to Credit Card payments.
 */
public interface CreditCardPaymentFacade extends PaymentFacade
{
    /**
     * Creates an instance of {@link PaymentData} which encapsulates create subscription request
     * based on selected strategy.
     *
     * @param responseUrl payment data response url
     * @return payment data object created through subscription strategy
     */
    PaymentData beginCreatePayment(final String responseUrl);

    /**
     * Completes creation of payment request.
     *
     * @param params payment request parameters
     * @param saveInAccount boolean flag that specifies whether or not payment info should be saved into account
     * @return resulting payment subscription data
     */
    PaymentSubscriptionResultData completeCreatePayment(final Map<String, String> params, boolean saveInAccount);

    /**
     * Authorizes credit card payment using Flex Microforms token for a given cart
     *
     * @param cart cart
     * @param flexToken Flex Microforms token
     * @return true if authorization succeeded, false otherwise
     */
    boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken);

    /**
     * Authorizes credit card payment, invoking authentication service, using Flex Microforms token for a given cart
     *
     * @param cart cart
     * @param flexToken Flex Microforms token
     * @return true if authorization succeeded, false otherwise
     */
    boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken, final String authJwt);

    /**
     * Authorizes credit card payment using Flex Microforms token for a given cart.
     * This is a follow on authorization after check enrollment allows to authorize without further validation
     *
     * @param cart cart
     * @param flexToken Flex Microforms token
     * @param enrollmentTransaction Initial enrollment check transaction.
     *                              This transaction should have reason code 100 indicating that authorization does not require validation
     * @return true if authorization succeeded, false otherwise
     */
    boolean authorizeFlexCreditCardPayment(final CartModel cart, final String flexToken,
            final IsvPaymentTransactionEntryModel enrollmentTransaction);

    /**
     * Creates a new JWT (tokens must be regenerated even if the last one has not expired)
     *
     * @return new JWT
     */
    String createEnrollmentJwt();

    /**
     * Does the check enrollment request for the given payment information
     *
     * @param referenceId Session ID provided on Cardinal 'payments.setupComplete' event
     * @param transientToken Flex microform token
     * @return
     */
    IsvPaymentTransactionEntryModel enrollCreditCard(final String referenceId, final String transientToken);

    /**
     * Checks if 3DS is enabled for the current merchant
     *
     * @return true if 3DS is enabled, false otherwise
     */
    boolean is3dsEnabled();
}
