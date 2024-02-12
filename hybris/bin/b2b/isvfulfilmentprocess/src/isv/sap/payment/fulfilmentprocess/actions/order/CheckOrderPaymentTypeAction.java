package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.utils.PaymentTransactionUtils;

import static de.hybris.platform.core.enums.OrderStatus.WAITING_FOR_PAYMENT;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class CheckOrderPaymentTypeAction extends AbstractAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CheckOrderPaymentTypeAction.class);

    private Map<PaymentType, Transition> paymentTypeMappings;

    @Override
    public String execute(final OrderProcessModel process) throws Exception // NOPMD
    {
        final OrderModel order = process.getOrder();

        if (order == null)
        {
            LOG.error("Missing the order, exiting the process");
            return Transition.NOK.toString();
        }

        final Optional<PaymentTransactionModel> transaction = PaymentTransactionUtils.
                getTransactionWithTheLatestEntry(order.getPaymentTransactions());

        if (transaction.isPresent())
        {
            final PaymentType paymentType = PaymentType.valueOf(transaction.get().getPaymentProvider());

            final Transition nextTransition = paymentTypeMappings.get(paymentType);

            if (nextTransition == Transition.ALTERNATIVE)
            {
                setOrderStatus(order, WAITING_FOR_PAYMENT);
            }

            return nextTransition.toString();
        }

        LOG.error("Missing the transaction on order [{}]", order.getGuid());
        return Transition.NOK.toString();
    }

    @Required
    public void setPaymentTypeMappings(final Map<PaymentType, Transition> paymentTypeMappings)
    {
        this.paymentTypeMappings = paymentTypeMappings;
    }

    @Override
    public Set<String> getTransitions()
    {
        return stream(Transition.values()).map(Transition::toString).collect(toSet());
    }

    protected enum Transition
    {
        ALTERNATIVE, CREDIT_CARD, ACCOUNT, VISA_CHECKOUT, NOK, APPLE_PAY, GOOGLE_PAY
    }
}
