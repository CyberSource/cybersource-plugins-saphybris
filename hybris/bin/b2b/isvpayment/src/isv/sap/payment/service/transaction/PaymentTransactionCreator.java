package isv.sap.payment.service.transaction;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

/**
 * Defines a component responsible for payment transaction entry creation.
 */
public interface PaymentTransactionCreator
{
    /**
     * Create and populate payment transaction entry with isv response data
     * for existing or new requested order payment transaction.
     *
     * @param request service request params
     * @param response isv response
     * @return created transaction entry
     */
    IsvPaymentTransactionEntryModel createTransactionEntry(
            final PaymentServiceRequest request,
            final PaymentServiceResult response);
}
