package isv.sap.payment.fulfilmentprocess.strategy.impl;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;

/**
 * Abstract implementation of {@link PaymentOperationStrategy}.
 * Encapsulates common logic for concrete take payment strategy implementations.
 */
public abstract class AbstractPaymentOperationStrategy implements PaymentOperationStrategy
{
    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public PaymentTransactionEntryModel execute(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        return paymentServiceExecutor.execute(request(order, transaction)).getData(TRANSACTION);
    }

    @Override
    public AlternativePaymentMethod getPaymentMethod()
    {
        return null;
    }

    protected abstract PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction);

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }

    public PaymentTransactionService getPaymentTransactionService()
    {
        return paymentTransactionService;
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }
}
