package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

import de.hybris.platform.util.Config;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.hybris.platform.core.enums.OrderStatus.PROCESSING_ERROR;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;

/**
 * This action implements payment authorization. Any other payment model could
 * be implemented here, or in a separate action, if the process flow differs.
 */
public class CheckAuthorizeOrderPaymentAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "authorizationReversalContext")
    private PaymentOperationContext authorizationReversalContext;

    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();
        return order != null ? assignStatusForOrder(order) : Transition.NOK;
    }

    /**
     * Sets the status for given order in case on of its {@link PaymentTransactionEntryModel} matches proper
     * {@link PaymentTransactionType} and {@link TransactionStatus}.
     *
     * @param order {@link OrderModel}
     * @return {@link Transition}
     */
    protected Transition assignStatusForOrder(final OrderModel order)
    {
        final Logger LOG = LoggerFactory.getLogger(TakePaymentAction.class);
        

            for (final PaymentTransactionModel transaction : order.getPaymentTransactions()) {

                final Optional<PaymentTransactionEntryModel> authorization = paymentTransactionService
                        .getLatestTransactionEntry(
                                transaction,
                                PaymentTransactionType.AUTHORIZATION,
                                IsvPaymentConstants.TransactionStatus.ACCEPT,
                                IsvPaymentConstants.TransactionStatus.REVIEW
                        );

                if (authorization.isPresent()) {
                    setOrderStatus(order, OrderStatus.PAYMENT_AUTHORIZED);
                  
                        return Transition.OK;
                  

                }
            }



        LOG.warn("The payment authorization has failed");
        return Transition.NOK;
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }
}
