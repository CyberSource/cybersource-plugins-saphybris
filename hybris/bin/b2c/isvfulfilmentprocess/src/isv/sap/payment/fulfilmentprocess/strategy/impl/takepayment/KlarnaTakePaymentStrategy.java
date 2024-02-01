package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.alternative.CaptureRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;

/**
 * An implementation of {@link PaymentOperationStrategy} interface for Klarna payment.
 */
public class KlarnaTakePaymentStrategy extends AbstractPaymentOperationStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.ALTERNATIVE_PAYMENT;
    }

    @Override
    public AlternativePaymentMethod getPaymentMethod()
    {
        return AlternativePaymentMethod.KLI;
    }

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel entryModel = (IsvPaymentTransactionEntryModel)
                getPaymentTransactionService()
                        .getLatestAcceptedTransactionEntry(transaction, PaymentTransactionType.AUTHORIZATION).get();

        return new CaptureRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, entryModel)
                .build();
    }
}
