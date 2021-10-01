package isv.sap.payment.service.alternativepayment.handler;

import de.hybris.platform.core.model.order.OrderModel;

/**
 * An interface that defines a handler for an order placed using Alternative payment.
 */
public interface AlternativePaymentOrderStatusHandler
{
    /**
     * Defines how an order is handled, based on payment status.
     *
     * @param order order object to handle
     */
    void handle(final OrderModel order);
}
