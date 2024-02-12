package isv.sap.payment.addon.facade;

import java.util.List;

import de.hybris.platform.acceleratorservices.payment.data.PaymentModeData;

import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.enums.PaymentType;

/**
 * This interface defines basic operations on payment modes.
 */
public interface PaymentModeFacade
{
    /**
     * Returns all allowed payment modes for current base store.
     *
     * @return a list fo available {@link PaymentModeData} instances for the store or an empty
     * list otherwise.
     */
    List<PaymentModeData> getPaymentModes();

    /**
     * Checks if given payment mode is supported by current store.
     *
     * @param paymentType payment type
     * @param paymentSubType payment sub type (for alternative payments)
     *
     * @return true is supported.
     */
    boolean isPaymentModeSupported(final PaymentType paymentType,
            final AlternativePaymentMethod paymentSubType);

    String getPaymentCountryCode();
}
