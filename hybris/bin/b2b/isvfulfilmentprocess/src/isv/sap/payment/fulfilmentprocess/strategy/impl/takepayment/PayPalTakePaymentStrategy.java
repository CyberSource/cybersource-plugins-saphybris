package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.paypal.CaptureRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.enums.AlternativePaymentMethod.PPL;
import static isv.cjl.payment.enums.PaymentType.PAY_PAL;

public class PayPalTakePaymentStrategy extends AbstractPaymentOperationStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PAY_PAL;
    }

    @Override
    public AlternativePaymentMethod getPaymentMethod()
    {
        return PPL;
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
                .addParam("isCaptureFinal", true)
                .build();
    }
}
