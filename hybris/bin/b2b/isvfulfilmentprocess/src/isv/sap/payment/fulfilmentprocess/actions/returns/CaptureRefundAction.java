package isv.sap.payment.fulfilmentprocess.actions.returns;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSAL_FAILED;
import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSED;
import static de.hybris.platform.payment.enums.PaymentTransactionType.REFUND_FOLLOW_ON;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;

/**
 * Mock implementation for refunding the money to the customer for the ReturnRequest
 */
public class CaptureRefundAction extends AbstractSimpleDecisionAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CaptureRefundAction.class);

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "refundPaymentContext")
    private PaymentOperationContext paymentOperationContext;

    @Override
    public Transition executeAction(final ReturnProcessModel process)
    {
        LOG.info("Process: " + process.getCode() + " in step " + getClass().getSimpleName());

        final ReturnRequestModel returnRequest = process.getReturnRequest();
        final OrderModel order = returnRequest.getOrder();

        try
        {
            for (final PaymentTransactionModel transaction : order.getPaymentTransactions())
            {
                final Optional<PaymentTransactionEntryModel> previousRefund = paymentTransactionService
                        .getLatestTransactionEntry(transaction, REFUND_FOLLOW_ON, ACCEPT);

                if (!previousRefund.isPresent())
                {
                    final PaymentTransactionEntryModel refund = paymentOperationContext
                            .strategy((IsvPaymentTransactionModel) transaction)
                            .execute(order, (IsvPaymentTransactionModel) transaction);

                    if (IsvPaymentConstants.TransactionStatus.ACCEPT.equals(refund.getTransactionStatus()))
                    {
                        LOG.debug("The payment has been refunded, order: [{}], txn: [{}]", order.getCode(),
                                refund.getCode());
                    }
                    else
                    {
                        LOG.error("The payment refund has failed, order: [{}], txn: [{}]", order.getCode(),
                                refund.getCode());
                        setReturnRequestStatus(returnRequest, PAYMENT_REVERSAL_FAILED);
                        return Transition.NOK;
                    }
                }
                else
                {
                    LOG.debug("Skipping refund for [{}]. Transaction already refunded", transaction.getCode());
                }
            }

            setReturnRequestStatus(returnRequest, PAYMENT_REVERSED);
            return Transition.OK;
        }
        catch (final Exception ex)
        {
            LOG.error("Refund failed, order: [{}]", order.getCode(), ex);
            setReturnRequestStatus(returnRequest, PAYMENT_REVERSAL_FAILED);
            return Transition.NOK;
        }
    }

    private void setReturnRequestStatus(final ReturnRequestModel returnRequest, final ReturnStatus returnStatus)
    {
        returnRequest.setStatus(returnStatus);
        returnRequest.getReturnEntries().stream().forEach(entry ->
        {
            entry.setStatus(returnStatus);
            getModelService().save(entry);
        });
        getModelService().save(returnRequest);
    }
}
