package isv.sap.payment.addon.facade;

import java.util.Optional;

import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData;

/**
 * This interface defines visa checkout payment details operations
 */
public interface VisaCheckoutPaymentDetailsFacade
{
    /**
     * Returns the visa checkout payment details for the current cart
     *
     * @return an Optional of {@link VisaCheckoutPaymentDetailsData} or empty Optional otherwise.
     */
    Optional<VisaCheckoutPaymentDetailsData> getVCPaymentDetails();
}
