package isv.sap.payment.dao;

import java.util.List;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.OrderDao;

/**
 * This class is intended to extend the OOTB OrderDao
 */
public interface PaymentOrderDao extends OrderDao
{
    /**
     * Find order instance by guid, which is an unique identifier
     *
     * @param guid order unique identifier
     * @return an order instance
     */
    OrderModel findOrderByGuid(String guid);

    /**
     * Finds orders by status.
     *
     * @param status the orders status
     * @return a list of orders that match provided status
     */
    List<OrderModel> findOrdersByStatus(OrderStatus status);
}
