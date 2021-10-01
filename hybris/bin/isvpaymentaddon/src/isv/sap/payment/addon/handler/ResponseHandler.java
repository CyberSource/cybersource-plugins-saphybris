package isv.sap.payment.addon.handler;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import de.hybris.platform.core.model.order.AbstractOrderModel;

/**
 * Interface defines various methods to check on isv response.
 */
public interface ResponseHandler
{
    /**
     * This method creates a map containing only signed parameters from the request + the signature.
     * All methods working with SOP/HOP responses should rely ONLY on the parameters returned here otherwise the application will be vulnerable to attacks.
     *
     * @param request SOP/HOP request
     * @return Map containing the signed request parameters + the signature
     */
    Map<String, String> getValidParameters(final HttpServletRequest request);

    /**
     * Processes isv response and creates order payment transactions.
     *
     * @param order the order
     * @param paymentResponse the payment response data
     */
    void processResponse(final AbstractOrderModel order, final Map<String, String> paymentResponse);

    /**
     *
     * @param paymentResponse Map containing the signed fields from the request. This map should be the output from getValidParameters
     * @return true/false indicating if the parameters have a valid signature. If the signature is not valid, the order should not be processed
     */
    boolean isValidSignature(final Map<String, String> paymentResponse);

    /**
     *
     * @param paymentResponse the payment response data
     * @return Indicates if the authorization for the payment was successful or not
     */
    boolean isDecisionSuccessful(final Map<String, String> paymentResponse);
}
