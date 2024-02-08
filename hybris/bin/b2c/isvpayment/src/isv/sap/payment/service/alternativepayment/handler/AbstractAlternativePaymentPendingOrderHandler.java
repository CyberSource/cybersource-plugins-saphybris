package isv.sap.payment.service.alternativepayment.handler;

import java.util.Locale;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.enums.IsvAlternativePaymentStatus;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService;

import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsResponseFields.PAYMENT_STATUS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * An abstract class implementation of {@link AbstractAlternativePaymentPendingOrderHandler} that encapsulates order status update for pending orders.
 */
public abstract class AbstractAlternativePaymentPendingOrderHandler implements AlternativePaymentOrderStatusHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAlternativePaymentPendingOrderHandler.class);

    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusService")
    private AlternativePaymentOrderStatusService alternativePaymentOrderStatusService;

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    protected abstract PaymentServiceRequest createCheckStatusRequestBuilder(final AbstractOrderModel order,
            final IsvPaymentTransactionModel transaction, final IsvPaymentTransactionEntryModel transactionEntry);

    @Override
    public void handle(final OrderModel order)
    {
        final IsvAlternativePaymentStatus paymentStatus = executeCheckStatusRequest(order);
        if (nonNull(paymentStatus))
        {
            alternativePaymentOrderStatusService.updateOrderByPaymentStatus(order, paymentStatus);
        }
        else
        {
            LOGGER.warn("No payment status got on Check Status transaction on order [{}]", order.getGuid());
        }
    }

    public IsvAlternativePaymentStatus executeCheckStatusRequest(final AbstractOrderModel order)
    {
        return resolvePaymentStatus(paymentServiceExecutor.execute(createCheckStatusRequest(order)));
    }

    private PaymentServiceRequest createCheckStatusRequest(final AbstractOrderModel order)
    {
        final IsvPaymentTransactionModel transaction =
                alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order);

        final IsvPaymentTransactionEntryModel transactionEntry =
                alternativePaymentOrderStatusService.getAlternativePaymentTransactionEntry(transaction);

        return createCheckStatusRequestBuilder(order, transaction, transactionEntry);
    }

    protected IsvAlternativePaymentStatus resolvePaymentStatus(final PaymentServiceResult result)
    {
        final IsvPaymentTransactionEntryModel checkStatusTransaction = result
                .getData(PaymentConstants.CommonFields.TRANSACTION);

        if (IsvPaymentConstants.ReasonCode.NON_EXISTING_TRANSACTION.equals(checkStatusTransaction.getProperties().get("reasonCode")))
        {
            return null;
        }

        final String apCheckStatusReplyPaymentStatus = checkStatusTransaction.getProperties().get(PAYMENT_STATUS);
        if (isNotBlank(apCheckStatusReplyPaymentStatus) && IsvPaymentConstants.TransactionStatus.ACCEPT.equals(checkStatusTransaction.getTransactionStatus()))
        {
            return IsvAlternativePaymentStatus.valueOf(apCheckStatusReplyPaymentStatus.toUpperCase(Locale.ENGLISH));
        }
        else if (IsvPaymentConstants.TransactionStatus.REJECT.equals(checkStatusTransaction.getTransactionStatus()))
        {
            return IsvAlternativePaymentStatus.REJECTED;
        }

        return null;
    }

    public void setAlternativePaymentOrderStatusService(
            final AlternativePaymentOrderStatusService alternativePaymentOrderStatusService)
    {
        this.alternativePaymentOrderStatusService = alternativePaymentOrderStatusService;
    }

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }

    public AlternativePaymentOrderStatusService getAlternativePaymentOrderStatusService()
    {
        return alternativePaymentOrderStatusService;
    }

    public PaymentServiceExecutor getPaymentServiceExecutor()
    {
        return paymentServiceExecutor;
    }
}
