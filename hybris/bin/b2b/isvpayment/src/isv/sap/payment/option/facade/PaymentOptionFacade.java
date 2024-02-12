package isv.sap.payment.option.facade;

import java.util.List;

import isv.cjl.payment.data.AlternativePaymentOptionData;

/**
 * This interface defines methods related to alternative payment options.
 */
public interface PaymentOptionFacade
{
    /**
     * Provides a list of available payment options.
     *
     * @return a list fo {@link AlternativePaymentOptionData} instances.
     */
    List<AlternativePaymentOptionData> getPaymentOptions();
}
