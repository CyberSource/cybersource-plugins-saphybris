package isv.sap.payment.addon.b2b.service;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;

import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

/**
 * Defines a customization of {@link PaymentTransactionService} for B2B.
 */
public interface B2bPaymentTransactionService extends PaymentTransactionService
{
    /**
     * Defines a method that returns latest accepted transaction entry for given payment
     * type, transaction type and order object
     *
     * @param paymentType transaction payment type
     * @param transactionType transaction type
     * @param order order object to look into
     * @return latest accepted transaction entry object
     */
    IsvPaymentTransactionEntryModel getLatestAcceptedTransactionEntry(final PaymentType paymentType,
            final PaymentTransactionType transactionType, final AbstractOrderModel order);
}
