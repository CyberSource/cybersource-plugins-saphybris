package isv.sap.payment.service.executor;

import de.hybris.platform.core.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.service.executor.DefaultPaymentServiceExecutor;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.transaction.PaymentTransactionCreatorContext;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;

public class CorePaymentServiceExecutor extends DefaultPaymentServiceExecutor
{
    private static final Logger LOG = LoggerFactory.getLogger(CorePaymentServiceExecutor.class);

    @Override
    public PaymentServiceResult execute(final PaymentServiceRequest request)
    {
        LOG.debug("executing payment service request from core library for request: {}", request);

        final isv.cjl.payment.service.executor.PaymentServiceResult result = executeSuper(request);

        final IsvPaymentTransactionEntryModel transactionEntry = getPaymentTransactionCreatorContext()
                .getPaymentTransactionCreator(request).createTransactionEntry(request, result);

        result.addData(TRANSACTION, transactionEntry);

        return result;
    }

    protected PaymentServiceResult executeSuper(final PaymentServiceRequest request)
    {
        return super.execute(request);
    }

    protected PaymentTransactionCreatorContext getPaymentTransactionCreatorContext()
    {
        return (PaymentTransactionCreatorContext) Registry.getApplicationContext()
                .getBean("isv.sap.payment.paymentTransactionCreatorContext");
    }
}
