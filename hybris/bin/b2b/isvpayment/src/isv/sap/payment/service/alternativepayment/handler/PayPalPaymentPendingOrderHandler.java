package isv.sap.payment.service.alternativepayment.handler;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.paypal.CheckStatusRequestBuilder;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;

/**
 * Paypal implementation of {@link PayPalPaymentPendingOrderHandler} that encapsulates logic for order status update for pending orders.
 */
public class PayPalPaymentPendingOrderHandler extends AbstractAlternativePaymentPendingOrderHandler
{
    @Override
    protected PaymentServiceRequest createCheckStatusRequestBuilder(final AbstractOrderModel order,
            final IsvPaymentTransactionModel transaction, final IsvPaymentTransactionEntryModel transactionEntry)
    {
        return new CheckStatusRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, transactionEntry)
                .build();
    }
}
