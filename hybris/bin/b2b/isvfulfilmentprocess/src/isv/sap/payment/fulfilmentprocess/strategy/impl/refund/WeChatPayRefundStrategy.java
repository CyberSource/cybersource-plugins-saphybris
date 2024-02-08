package isv.sap.payment.fulfilmentprocess.strategy.impl.refund;

import de.hybris.platform.core.model.order.OrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.alternative.RefundRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static de.hybris.platform.payment.enums.PaymentTransactionType.SALE;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.enums.AlternativePaymentMethod.WQR;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static java.math.BigDecimal.valueOf;

public class WeChatPayRefundStrategy extends AbstractPaymentOperationStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.ALTERNATIVE_PAYMENT;
    }

    @Override
    public AlternativePaymentMethod getPaymentMethod()
    {
        return WQR;
    }

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel transactionEntry = (IsvPaymentTransactionEntryModel)
                getPaymentTransactionService().getLatestTransactionEntry(transaction, SALE, ACCEPT).get();

        final RefundRequestBuilder builder = new RefundRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .setAmount(valueOf(order.getTotalPrice()))
                .addParam(ORDER, order)
                .addParam("alternatePaymentMethod", WQR)
                .addParam(TRANSACTION, transactionEntry);

        return builder.build();
    }
}
