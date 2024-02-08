package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.google.common.collect.Lists;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.constants.PaymentConstants.ReasonCode;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.model.IsvPaymentTransactionModel;

/**
 * The action processes duplicate authorization record for the order and fetches first successful transaction from
 * isv. The first transaction data is used to process the order further.
 */
public class ProcessDuplicatedAuthorizationAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(ProcessDuplicatedAuthorizationAction.class);

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Override
    public Transition executeAction(final OrderProcessModel orderProcessModel) throws RetryLaterException
    {
        final OrderModel order = orderProcessModel.getOrder();

        final List<PaymentTransactionEntryModel> duplicateAuthorizeTransactionEntries = getDuplicateAuthorizeTransactionEntries(
                order);

        if (!duplicateAuthorizeTransactionEntries.isEmpty())
        {
            return tryProcessDuplicateAuthorizationTransaction(order, duplicateAuthorizeTransactionEntries);
        }

        return Transition.OK;
    }

    private Transition tryProcessDuplicateAuthorizationTransaction(final OrderModel order,
            final List<PaymentTransactionEntryModel> paymentTransactionEntries)
    {
        LOG.info("Processing duplicate authorization record for order [{}]. Fetching first successful transaction...",
                order.getGuid());
        try
        {
            return processDuplicateAuthorizationTransaction(order, paymentTransactionEntries);
        }
        catch (final Exception e)
        {
            LOG.error("Unable to retrieve the transaction details for order [{}].", order.getGuid());
            LOG.error(e.getMessage(), e);
            final RetryLaterException exception = new RetryLaterException(e.getMessage(), e);
            exception.setMethod(RetryLaterException.Method.LINEAR);
            throw exception;
        }
    }

    private Transition processDuplicateAuthorizationTransaction(final OrderModel order,
            final List<PaymentTransactionEntryModel> oldPaymentTransactionEntries)
    {
        for (final PaymentTransactionEntryModel entry : oldPaymentTransactionEntries)
        {
            createNewAuthorizationTransactions(order, entry.getPaymentTransaction());
        }
        return Transition.OK;
    }

    private void createNewAuthorizationTransactions(final OrderModel order, final PaymentTransactionModel transaction)
    {
        final PaymentServiceRequest request = PaymentServiceRequest.create()
                .service(isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION)
                .source(isv.cjl.payment.enums.PaymentSource.REPORTING)
                .method(PaymentType.valueOf(transaction.getPaymentProvider()))
                .addParam(PaymentConstants.CommonFields.MERCHANT_ID,
                        ((IsvPaymentTransactionModel) transaction).getMerchantId())
                .addParam(PaymentConstants.CommonFields.ORDER, order)
                .addParam("paymentTransaction", transaction);

        paymentServiceExecutor.execute(request);
    }

    private List<PaymentTransactionEntryModel> getDuplicateAuthorizeTransactionEntries(final OrderModel orderModel)
    {
        final List<PaymentTransactionEntryModel> transactions = Lists.newArrayList();
        orderModel.getPaymentTransactions().forEach((transaction) ->
                transactions.addAll(transaction.getEntries().stream().filter
                        (this::isDuplicateAuthorizeTransactionEntry).collect(Collectors.toList())));

        return transactions;
    }

    private boolean isDuplicateAuthorizeTransactionEntry(final PaymentTransactionEntryModel transactionEntry)
    {
        return transactionEntry.getType() == PaymentTransactionType.AUTHORIZATION
                && ReasonCode.DUPLICATE_TRANSACTION.equals(transactionEntry.getTransactionStatusDetails());
    }
}
