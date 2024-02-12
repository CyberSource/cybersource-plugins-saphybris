package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.core.enums.OrderStatus.CANCELLED;
import static de.hybris.platform.core.enums.OrderStatus.PROCESSING_ERROR;
import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;

/**
 * Cancels the order. The code expects no previously captured payment transaction, otherwise the order is
 * set to PROCESSING_ERROR status. Authorized transactions are reversed.
 */
public class CancelWholeOrderAuthorizationAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CancelWholeOrderAuthorizationAction.class);

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "authorizationReversalContext")
    private PaymentOperationContext authorizationReversalContext;

    @Override
    public void executeAction(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();

        try
        {
            LOG.debug("The order [{}] is being cancelled", order.getCode());

            for (final PaymentTransactionModel transaction : order.getPaymentTransactions())
            {
                final Optional<PaymentTransactionEntryModel> capture = paymentTransactionService
                        .getLatestAcceptedTransactionEntry(transaction,
                                CAPTURE);
                final Optional<PaymentTransactionEntryModel> auth = paymentTransactionService
                        .getLatestAcceptedTransactionEntry(transaction,
                                AUTHORIZATION);

                if (capture.isPresent())
                {
                    LOG.error("Cancel action failed: capture transaction(s) exist for the order [{}]", order.getCode());
                    setOrderStatus(order, PROCESSING_ERROR);
                    return;
                }

                if (auth.isPresent())
                {
                    LOG.debug("Reversing authorization for order [{}], transaction [{}]", order.getCode(),
                            transaction.getCode());
                    final PaymentTransactionEntryModel authReversal = authorizationReversalContext
                            .strategy((IsvPaymentTransactionModel) transaction)
                            .execute(order, (IsvPaymentTransactionModel) transaction);

                    if (!ACCEPT.equals(authReversal.getTransactionStatus()))
                    {
                        LOG.error(
                                "Cancel action failed: authorization reversal for order [{}], transaction [{}] failed",
                                order.getCode(), transaction.getCode());
                        setOrderStatus(order, PROCESSING_ERROR);
                        return;
                    }
                }
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Cancel action failed for order [{}] with error [{}]", order.getCode(), ex.getMessage(),
                    ex.getCause());
            setOrderStatus(order, PROCESSING_ERROR);
        }

        setOrderStatus(order, CANCELLED);
        LOG.debug("Order [{}] has been cancelled", order.getCode());
    }
}
