package isv.sap.payment.service.alternativepayment;

import java.util.Map;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;

import isv.cjl.payment.enums.CheckStatusDecision;
import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.service.alternativepayment.handler.AlternativePaymentOrderStatusHandler;

/**
 * Context class that supports alternative payment order handling.
 */
public class AlternativePaymentOrderStatusContext
{
    @Resource(name = "isv.sap.payment.alternativePaymentAbandonedOrderHandler")
    private AlternativePaymentOrderStatusHandler abandonedOrderHandler;

    @Resource(name = "isv.sap.payment.defaultPaymentPendingOrderHandler")
    private AlternativePaymentOrderStatusHandler defaultPendingOrderHandler;

    private Map<AlternativePaymentMethod, AlternativePaymentOrderStatusHandler> pendingOrderHandlersMap;

    /**
     * Provides an instance of {@link AlternativePaymentOrderStatusHandler} that handles
     * alternative payment order based on status.
     *
     * @param checkStatusDecision
     * @param alternativePaymentMethod
     * @return order status handler
     */
    public AlternativePaymentOrderStatusHandler getOrderHandler(final CheckStatusDecision checkStatusDecision,
            final AlternativePaymentMethod alternativePaymentMethod)
    {
        if (!CheckStatusDecision.RUN.equals(checkStatusDecision))
        {
            return abandonedOrderHandler;
        }
        return pendingOrderHandlersMap.getOrDefault(alternativePaymentMethod, defaultPendingOrderHandler);
    }

    @Required
    public void setPendingOrderHandlersMap(
            final Map<AlternativePaymentMethod, AlternativePaymentOrderStatusHandler> pendingOrderHandlersMap)
    {
        this.pendingOrderHandlersMap = pendingOrderHandlersMap;
    }
}
