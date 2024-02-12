package isv.sap.payment.option.service;

import java.util.List;

import isv.sap.payment.model.IsvAlternativePaymentOptionModel;

/**
 * This interface defines basic operations on {@link IsvAlternativePaymentOptionModel} objects.
 */
public interface PaymentOptionService
{
    /**
     * Returns all available {@link IsvAlternativePaymentOptionModel} instances.
     *
     * @return a list of {@link IsvAlternativePaymentOptionModel} instances.
     */
    List<IsvAlternativePaymentOptionModel> getPaymentOptions();

    /**
     * Updates all available {@link IsvAlternativePaymentOptionModel} instances.
     * Existing alternative payment options are replaced by provided new options.
     *
     * @param options new {@link IsvAlternativePaymentOptionModel} instances.
     */
    void updatePaymentOptions(final List<IsvAlternativePaymentOptionModel> options);
}
