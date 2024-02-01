package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.builder.ProcessingLevelPaymentServiceRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.applepay.CaptureRequestBuilder;

public class ApplePayTakePaymentStrategy extends AbstractCreditCardTakePaymentStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.APPLE_PAY;
    }

    @Override
    ProcessingLevelPaymentServiceRequestBuilder getBuilder()
    {
        return new CaptureRequestBuilder();
    }
}
