package isv.sap.payment.service.transaction;

import javax.annotation.Resource;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;

public class PaymentTransactionCreatorContext
{
    @Resource(name = "isv.sap.payment.transientPaymentTransactionCreator")
    private PaymentTransactionCreator transientPaymentTransactionCreator;

    @Resource(name = "isv.sap.payment.persistentPaymentTransactionCreator")
    private PaymentTransactionCreator persistentPaymentTransactionCreator;

    public PaymentTransactionCreator getPaymentTransactionCreator(final PaymentServiceRequest request)
    {
        return shouldPersist(request) ? persistentPaymentTransactionCreator : transientPaymentTransactionCreator;
    }

    private boolean shouldPersist(final PaymentServiceRequest request)
    {
        return request.getRequestParams().get("order") != null;
    }
}
