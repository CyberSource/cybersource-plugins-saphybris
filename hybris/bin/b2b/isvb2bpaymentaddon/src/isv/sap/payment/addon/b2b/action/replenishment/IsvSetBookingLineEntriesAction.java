package isv.sap.payment.addon.b2b.action.replenishment;

import de.hybris.platform.b2b.process.approval.actions.SetBookingLineEntries;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.task.RetryLaterException;

import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.ACCOUNT;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK;

/**
 * Execute SetBookingLineEntries only for B2B ACCOUNT payment type.
 */
public class IsvSetBookingLineEntriesAction extends SetBookingLineEntries
{
    @Override
    public Transition executeAction(final B2BApprovalProcessModel process) throws RetryLaterException
    {
        final OrderModel order = getOrder(process);

        return ACCOUNT.equals(order.getPaymentType()) ? callSupper(process) : OK;
    }

    private OrderModel getOrder(final B2BApprovalProcessModel process)
    {
        final OrderModel order = process.getOrder();

        modelService.refresh(order);

        return order;
    }

    protected Transition callSupper(final B2BApprovalProcessModel process)
    {
        return super.executeAction(process);
    }
}
