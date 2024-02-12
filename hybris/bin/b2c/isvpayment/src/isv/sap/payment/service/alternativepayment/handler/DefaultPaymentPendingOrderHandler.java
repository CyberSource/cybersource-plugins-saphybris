package isv.sap.payment.service.alternativepayment.handler;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;

/**
 * A default implementation of {@link DefaultPaymentPendingOrderHandler} that encapsulates default logic for order status update for pending orders.
 */
public class DefaultPaymentPendingOrderHandler extends AbstractAlternativePaymentPendingOrderHandler
{
    @Override
    protected PaymentServiceRequest createCheckStatusRequestBuilder(final AbstractOrderModel order,
            final IsvPaymentTransactionModel transaction, final IsvPaymentTransactionEntryModel transactionEntry)
    {
        return new CheckStatusRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, transactionEntry)
                .addParam("alternatePaymentMethod",
                        AlternativePaymentMethod.valueOf(transaction.getAlternativePaymentMethod().getCode()))
                .addParam("paymentTransactionType",
                        PaymentTransactionType.valueOf(transactionEntry.getType().getCode()))
                .build();
    }
}
