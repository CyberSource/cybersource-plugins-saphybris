package isv.sap.payment.commerceservices.order.dao;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import de.hybris.platform.commerceservices.order.dao.impl.DefaultCommerceCartDao;
import de.hybris.platform.core.model.order.CartModel;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class DefaultPaymentCartDao extends DefaultCommerceCartDao implements PaymentCartDao
{
    private static final String FIND_CART_FOR_GUID = SELECTCLAUSE + "WHERE {" + CartModel.GUID + "} = ?guid ";

    @Override
    public CartModel getCartForGuid(final String guid)
    {
        final Map<String, Object> params = ImmutableMap.<String, Object>builder().put("guid", guid).build();

        final List<CartModel> carts = doSearch(FIND_CART_FOR_GUID, params, CartModel.class);

        return isNotEmpty(carts) ? carts.get(0) : null;
    }
}
