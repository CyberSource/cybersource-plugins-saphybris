package isv.sap.payment.addon.b2b.service;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.DefaultPaymentTransactionService;

/**
 * Encapsulates the default implementation of {@link B2bPaymentTransactionService} interface.
 *
 * <p>
 * Acts as a simple wrapper around {@link DefaultPaymentTransactionService} logic for B2B scenarios.
 */
public class DefaultB2bPaymentTransactionService extends DefaultPaymentTransactionService
        implements B2bPaymentTransactionService
{
    @Override
    public IsvPaymentTransactionEntryModel getLatestAcceptedTransactionEntry(final PaymentType paymentType,
            final PaymentTransactionType transactionType, final AbstractOrderModel order)
    {
        final PaymentTransactionModel transaction = super.getTransaction(paymentType, order).get();
        return (IsvPaymentTransactionEntryModel) getLatestAcceptedTransactionEntry(transaction, transactionType).get();
    }
}
