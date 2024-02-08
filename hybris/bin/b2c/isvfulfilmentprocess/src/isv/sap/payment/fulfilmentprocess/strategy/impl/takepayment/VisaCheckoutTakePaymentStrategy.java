package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.OrderModel;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.visacheckout.CaptureRequestBuilder;
import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW;

/**
 * An implementation of {@link PaymentOperationStrategy} interface for Visa Checkout payment.
 */
public class VisaCheckoutTakePaymentStrategy extends AbstractPaymentOperationStrategy
{
    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public PaymentType getPaymentType()
    {
        return VISA_CHECKOUT;
    }

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final String vcOrderId = authorization(transaction).getProperties().get("vcOrderId");

        return new CaptureRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(TRANSACTION, authorization(transaction))
                .addParam(ORDER, order)
                .setVcOrderId(vcOrderId)
                .build();
    }

    private IsvPaymentTransactionEntryModel authorization(final IsvPaymentTransactionModel transaction)
    {
        return (IsvPaymentTransactionEntryModel) paymentTransactionService.
                getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW).get();
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }
}
