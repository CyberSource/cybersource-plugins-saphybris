package isv.sap.payment.service.alternativepayment.handler;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.OrderModel;

import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService;

import static isv.sap.payment.enums.IsvAlternativePaymentStatus.ABANDONED;

/**
 * An implementation of {@link AlternativePaymentOrderStatusHandler} that encapsulates order
 * status update for an abandoned orders.
 */
public class AlternativePaymentAbandonedOrderHandler implements AlternativePaymentOrderStatusHandler
{
    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusService")
    private AlternativePaymentOrderStatusService alternativePaymentOrderStatusService;

    @Override
    public void handle(final OrderModel order)
    {
        alternativePaymentOrderStatusService.updateOrderByPaymentStatus(order, ABANDONED);
    }
}
