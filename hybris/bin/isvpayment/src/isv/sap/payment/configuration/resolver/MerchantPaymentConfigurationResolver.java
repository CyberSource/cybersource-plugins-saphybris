package isv.sap.payment.configuration.resolver;

import java.util.Collection;
import java.util.Map;

import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel;

import static de.hybris.platform.core.model.ItemModel.PK;
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel._TYPECODE;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

/**
 * This class is responsible for retrieving merchant payment configuration
 */
public class MerchantPaymentConfigurationResolver
        extends AbstractPaymentConfigurationResolver<IsvMerchantPaymentConfigurationModel>
{
    private static final String QUERY_TEMPLATE =
            "SELECT {" + PK + "} FROM {" + _TYPECODE + "} WHERE %s";

    @Override
    public String getSearchQuery(final Map<String, Object> params)
    {
        final Collection<String> keys = params.keySet().stream().map(key -> format("{%s}=?%s", key, key))
                .collect(toList());
        return format(QUERY_TEMPLATE, join(" AND ", keys));
    }
}
