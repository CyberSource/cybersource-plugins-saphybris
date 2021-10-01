package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.builder.ProcessingLevelPaymentServiceRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.googlepay.CaptureRequestBuilder;

public class GooglePayTakePaymentStrategy extends AbstractCreditCardTakePaymentStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.GOOGLE_PAY;
    }

    @Override
    ProcessingLevelPaymentServiceRequestBuilder getBuilder()
    {
        return new CaptureRequestBuilder();
    }
}
