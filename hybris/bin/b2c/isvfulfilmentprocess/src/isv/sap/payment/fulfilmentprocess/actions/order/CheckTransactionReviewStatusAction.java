package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.enums.CsTicketPriority;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.util.localization.Localization;
import org.springframework.beans.factory.annotation.Required;

public class CheckTransactionReviewStatusAction extends AbstractAction<OrderProcessModel>
{
    private TicketBusinessService ticketBusinessService;

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }

    @Override
    public final String execute(final OrderProcessModel process)
    {
        return executeAction(process).toString();
    }

    protected Transition executeAction(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();
        if (order != null)
        {
            for (final PaymentTransactionModel transaction : order.getPaymentTransactions())
            {
                final Transition result = checkPaymentTransaction(transaction, order);
                if (!Transition.OK.equals(result))
                {
                    return result;
                }
            }
        }

        return Transition.OK;
    }

    private Transition checkPaymentTransaction(final PaymentTransactionModel transaction, final OrderModel order)
    {
        if (isOrderInReviewState(transaction))
        {
            final FraudStatus fraudStatus = getFraudDecisionStatus(order);
            if (FraudStatus.OK.equals(fraudStatus))
            {
                setOrderStatus(order, OrderStatus.PAYMENT_AUTHORIZED);
                return Transition.OK;
            }
            else if (FraudStatus.FRAUD.equals(fraudStatus))
            {
                setOrderStatus(order, OrderStatus.PAYMENT_NOT_AUTHORIZED);
                return Transition.NOK;
            }
            else
            {
                createFraudTicket(order);
                setOrderStatus(order, OrderStatus.FRAUD_DECISION);
                return Transition.WAIT;
            }
        }

        return Transition.OK;
    }

    private boolean isOrderInReviewState(final PaymentTransactionModel transaction)
    {
        return transaction.getEntries().stream()
                .filter(this::isAuthorizationInReview)
                .map(entry -> true)
                .findFirst()
                .orElse(false);
    }

    private void createFraudTicket(final OrderModel order)
    {
        final String ticketTitle = getLocalizedString("message.ticket.orderinreview.title");
        final String ticketMessage = getLocalizedString("message.ticket.orderinreview.content", order.getCode());

        final CsTicketModel newTicket = modelService.create(CsTicketModel.class);
        newTicket.setHeadline(ticketTitle);
        newTicket.setCategory(CsTicketCategory.FRAUD);
        newTicket.setPriority(CsTicketPriority.HIGH);
        newTicket.setOrder(order);
        newTicket.setCustomer(order.getUser());

        final CsCustomerEventModel newTicketEvent = new CsCustomerEventModel();
        newTicketEvent.setText(ticketMessage);

        ticketBusinessService.createTicket(newTicket, newTicketEvent);
    }

    private FraudStatus getFraudDecisionStatus(final OrderModel order)
    {
        return order.getFraudReports().stream()
                .sorted(Comparator.comparing(FraudReportModel::getTimestamp).reversed())
                .findFirst()
                .map(FraudReportModel::getStatus)
                .orElse(null);
    }

    private boolean isAuthorizationInReview(final PaymentTransactionEntryModel entry)
    {
        return PaymentTransactionType.AUTHORIZATION.equals(entry.getType())
                && TransactionStatus.REVIEW.name().equals(entry.getTransactionStatus());
    }

    @Required
    public void setTicketBusinessService(final TicketBusinessService ticketBusinessService)
    {
        this.ticketBusinessService = ticketBusinessService;
    }

    protected String getLocalizedString(final String key, final Object... params)
    {
        return Localization.getLocalizedString(key, params);
    }

    public enum Transition
    {
        OK, NOK, WAIT;

        public static Set<String> getStringValues()
        {
            final Set<String> res = new HashSet<String>();
            for (final Transition transitions : Transition.values())
            {
                res.add(transitions.toString());
            }
            return res;
        }
    }
}
