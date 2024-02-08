package isv.sap.payment.report.listener.conversion.decision;

import de.hybris.platform.core.model.order.OrderModel;

/**
 * Defines an interface responsible for updating order fraud status
 */
public interface DecisionChangeStrategy
{
    void updateOrderFraudStatus(final OrderModel order);

    boolean supports(final String decision);
}
