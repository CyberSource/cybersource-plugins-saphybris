package isv.sap.payment.addon.b2b.action.approval;

import de.hybris.platform.b2b.process.approval.actions.AbstractSimpleB2BApproveOrderDecisionAction;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.task.RetryLaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckPaymentIsNotAccountBasedAction extends AbstractSimpleB2BApproveOrderDecisionAction
{
    private static final Logger LOG = LoggerFactory.getLogger(CheckPaymentIsNotAccountBasedAction.class);

    @Override
    public Transition executeAction(final B2BApprovalProcessModel process) throws RetryLaterException
    {
        OrderModel order = null;
        Transition transition = Transition.NOK;
        try
        {
            order = process.getOrder();

            if (!CheckoutPaymentType.ACCOUNT.equals(order.getPaymentType()))
            {
                transition = Transition.OK;
            }
        }
        catch (final Exception e)
        {
            this.handleError(order, e);
        }
        return transition;
    }

    protected void handleError(final OrderModel order, final Exception exception)
    {
        if (order != null)
        {
            this.setOrderStatus(order, OrderStatus.B2B_PROCESSING_ERROR);
        }
        LOG.error(exception.getMessage(), exception);
    }
}
