package isv.sap.payment.fulfilmentprocess.strategy.impl.refund;

import de.hybris.platform.core.model.order.OrderModel;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.creditcard.RefundFollowOnRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;

public class CreditCardRefundStrategy extends AbstractPaymentOperationStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.CREDIT_CARD;
    }

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel transactionEntry = (IsvPaymentTransactionEntryModel)
                getPaymentTransactionService().getLatestTransactionEntry(transaction, CAPTURE, ACCEPT).get();

        final RefundFollowOnRequestBuilder builder = new RefundFollowOnRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .setAmount(transactionEntry.getAmount())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, transactionEntry);

        return builder.build();
    }
}
