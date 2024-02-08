package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.request.builder.ProcessingLevelPaymentServiceRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.creditcard.CaptureRequestBuilder;

public class CreditCardTakePaymentStrategy extends AbstractCreditCardTakePaymentStrategy
{
    @Override
    public PaymentType getPaymentType()
    {
        return PaymentType.CREDIT_CARD;
    }

    @Override
    ProcessingLevelPaymentServiceRequestBuilder getBuilder()
    {
        return new CaptureRequestBuilder();
    }
}
