package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.fulfilmentprocess.strategy.context.PaymentOperationContext;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_CAPTURED;
import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_NOT_CAPTURED;
import static de.hybris.platform.core.enums.OrderStatus.PROCESSING_ERROR;
import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsResponseFields.Sale.PAYMENT_STATUS;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW;
import static isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction.Transition.NOK;
import static isv.sap.payment.fulfilmentprocess.actions.order.TakePaymentAction.Transition.OK;
import static isv.sap.payment.utils.Assert.isTrue;
import static isv.sap.payment.utils.PaymentTransactionUtils.isAlternativePayment;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.apache.logging.log4j.util.Strings.EMPTY;

/**
 * The TakePayment step captures the payment transaction.
 */
public class TakePaymentAction extends AbstractAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(TakePaymentAction.class);

    @Resource(name = "takePaymentContext")
    private PaymentOperationContext paymentOperationContext;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    private Map<String, OrderStatus> alternativePaymentOrderStatusMap;

    private Map<OrderStatus, Transition> orderStatusTransitionMap;

    @Override
    public String execute(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();
        String checkAuth = Config.getString("isv.payment.paymentAcceptance.type", StringUtils.EMPTY);
        try
        {
            for (final PaymentTransactionModel transaction : order.getPaymentTransactions())
            {
                final Optional<PaymentTransactionEntryModel> auth = paymentTransactionService
                        .getLatestTransactionEntry(transaction,
                                AUTHORIZATION, ACCEPT, REVIEW);

                if (auth.isPresent())
                {
                    if(checkAuth.compareToIgnoreCase("auth")==0){
                        setOrderStatus(order, OrderStatus.PAYMENT_AUTHORIZED);
                        return NOK.name();
                    }
                    final PaymentTransactionEntryModel capture = paymentOperationContext
                            .strategy((IsvPaymentTransactionModel) transaction)
                            .execute(order, (IsvPaymentTransactionModel) transaction);

                    final OrderStatus orderStatus = getOrderStatusFromCapture(capture);
                    setOrderStatus(order, orderStatus);

                    LOG.debug("The order {} status updated to {} from capture txn: {}", orderStatus.getCode(),
                            order.getCode(), capture.getCode());

                    if (orderStatus != PAYMENT_CAPTURED)
                    {
                        LOG.warn("The payment transaction capture wasn't successful, order: {}, txn: {}",
                                order.getCode(), capture.getCode());

                        return orderStatusTransitionMap.get(orderStatus).name();
                    }
                }
            }

            return OK.name();
        }
        catch (final Exception ex)
        {
            LOG.error("Take payment action failed for order [{}] with error [{}]", order.getCode(), ex.getMessage(),
                    ex.getCause());

            setOrderStatus(order, PROCESSING_ERROR);
            return NOK.name();
        }
    }

    protected OrderStatus getOrderStatusFromCapture(final PaymentTransactionEntryModel capture)
    {
        if (ACCEPT.equals(capture.getTransactionStatus()))
        {
            if (isAlternativePayment(capture.getPaymentTransaction()))
            {
                final String paymentStatus = ((IsvPaymentTransactionEntryModel) capture).getProperties()
                        .getOrDefault(PAYMENT_STATUS, EMPTY)
                        .toUpperCase(Locale.ENGLISH);

                final OrderStatus orderStatus = alternativePaymentOrderStatusMap.get(paymentStatus);

                isTrue(orderStatus != null,
                    () -> new IllegalArgumentException(
                        format("No order status mapping is defined for alternative payment status [%s]",
                            paymentStatus)));

                return orderStatus;
            }
            else
            {
                return PAYMENT_CAPTURED;
            }
        }
        else
        {
            return PAYMENT_NOT_CAPTURED;
        }
    }

    public void setPaymentOperationContext(final PaymentOperationContext paymentOperationContext)
    {
        this.paymentOperationContext = paymentOperationContext;
    }

    @Required
    public void setAlternativePaymentOrderStatusMap(final Map<String, OrderStatus> alternativePaymentOrderStatusMap)
    {
        this.alternativePaymentOrderStatusMap = alternativePaymentOrderStatusMap;
    }

    @Required
    public void setOrderStatusTransitionMap(final Map<OrderStatus, Transition> orderStatusTransitionMap)
    {
        this.orderStatusTransitionMap = orderStatusTransitionMap;
    }

    @Override
    public Set<String> getTransitions()
    {
        return stream(Transition.values()).map(Transition::toString).collect(toSet());
    }

    protected enum Transition
    {
        OK, NOK, WAIT
    }
}
