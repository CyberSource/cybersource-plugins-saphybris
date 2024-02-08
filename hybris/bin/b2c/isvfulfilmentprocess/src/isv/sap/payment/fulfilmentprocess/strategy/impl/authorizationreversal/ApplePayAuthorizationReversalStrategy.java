package isv.sap.payment.fulfilmentprocess.strategy.impl.authorizationreversal;

import de.hybris.platform.core.model.order.OrderModel;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.applepay.AuthorizationReversalRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW;

public class ApplePayAuthorizationReversalStrategy extends AbstractPaymentOperationStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.APPLE_PAY;
    }

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel entryModel = (IsvPaymentTransactionEntryModel)
                getPaymentTransactionService().getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW)
                        .get();

        return new AuthorizationReversalRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, entryModel)
                .build();
    }
}
