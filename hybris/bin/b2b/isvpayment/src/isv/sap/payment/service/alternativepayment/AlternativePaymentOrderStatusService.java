package isv.sap.payment.service.alternativepayment;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.CheckStatusDecision;
import isv.sap.payment.enums.IsvAlternativePaymentStatus;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

/**
 * Defines operations related to alternative payment status and order transactions.
 * <p>
 * Existing methods operate on both order and transaction level.
 */
public interface AlternativePaymentOrderStatusService
{
    /**
     * Determines if there is a need to run or skip the check status request
     *
     * @param transaction transaction to check
     * @return check status decision
     */
    CheckStatusDecision shouldRunCheckStatus(final IsvPaymentTransactionModel transaction);

    /**
     * Updates order status to provided value.
     *
     * @param order order to update
     * @param alternativePaymentStatus status to set
     */
    void updateOrderByPaymentStatus(final AbstractOrderModel order,
            final IsvAlternativePaymentStatus alternativePaymentStatus);

    /**
     * Returns alternative payment transaction for provided order (the payment provider is ALTERNATIVE_PAYMENT or PAY_PAL).
     *
     * @param order to look for transaction
     * @return alternative payment transaction
     */
    IsvPaymentTransactionModel getAlternativePaymentTransaction(final AbstractOrderModel order);

    /**
     * Returns latest alternative payment transaction entry for provided transaction.
     *
     * @param transaction to look for transaction entry
     * @return alternative payment transaction entry
     */
    IsvPaymentTransactionEntryModel getAlternativePaymentTransactionEntry(
            final IsvPaymentTransactionModel transaction);

    /**
     * Returns the counter that reflects how many times alternative payment status
     * was checked on transaction.
     *
     * @param transaction alternative payment transaction
     * @return alternative payment status check counter
     */
    int getExistingCheckStatusAttempts(final IsvPaymentTransactionModel transaction);
}
