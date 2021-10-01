package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;

import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsResponseFields.Sale.PAYMENT_STATUS;
import static isv.sap.payment.enums.IsvAlternativePaymentStatus.AUTHORIZED;
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativeAuthorizeOrderPaymentAction.Transition.OK;
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativeAuthorizeOrderPaymentAction.Transition.WAIT;
import static java.util.stream.Collectors.toSet;

public class CheckAlternativeAuthorizeOrderPaymentAction extends AbstractAction<OrderProcessModel>
{
    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusService")
    private AlternativePaymentOrderStatusService alternativePaymentOrderStatusService;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public String execute(final OrderProcessModel process)
    {
        final Optional<PaymentTransactionEntryModel> authOptional = getLatestAuthorization(process.getOrder());

        if (authOptional.isPresent() && isAuthorized((IsvPaymentTransactionEntryModel) authOptional.get()))
        {
            return OK.name();
        }
        else
        {
            return WAIT.name();
        }
    }

    private Optional<PaymentTransactionEntryModel> getLatestAuthorization(final OrderModel order)
    {
        final PaymentTransactionModel transaction = alternativePaymentOrderStatusService
                .getAlternativePaymentTransaction(order);

        return paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, AUTHORIZATION);
    }

    private boolean isAuthorized(final IsvPaymentTransactionEntryModel entry)
    {
        return AUTHORIZED.getCode().equalsIgnoreCase(entry.getProperties().get(PAYMENT_STATUS));
    }

    @Override
    public Set<String> getTransitions()
    {
        return Arrays.stream(Transition.values()).map(Transition::toString).collect(toSet());
    }

    protected enum Transition
    {
        OK, WAIT
    }
}
