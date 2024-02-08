package isv.sap.payment.dao;

import java.util.List;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.impl.DefaultOrderDao;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class DefaultPaymentOrderDao extends DefaultOrderDao implements PaymentOrderDao
{
    private static final String SELECT_ORDER_BY_CODE_QUERY =
            "SELECT {" + OrderModel.PK + "} FROM {" + OrderModel._TYPECODE
                    + "} WHERE {" + OrderModel.GUID + "} = ?guid";

    private static final String SELECT_ORDER_BY_STATUS_QUERY =
            "SELECT {" + OrderModel.PK + "} FROM {" + OrderModel._TYPECODE
                    + "} WHERE {" + OrderModel.STATUS + "} = ?status";

    @Override
    public OrderModel findOrderByGuid(final String guid)
    {
        return this.<OrderModel>search(SELECT_ORDER_BY_CODE_QUERY,
                ImmutableMap.of("guid", guid)).getResult()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ModelNotFoundException("Unable to found order with code: " + guid));
    }

    @Override
    public List<OrderModel> findOrdersByStatus(final OrderStatus status)
    {
        return this.<OrderModel>search(SELECT_ORDER_BY_STATUS_QUERY, ImmutableMap.of("status", status))
                .getResult();
    }
}
