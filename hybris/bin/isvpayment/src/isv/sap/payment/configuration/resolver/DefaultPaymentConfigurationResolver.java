package isv.sap.payment.configuration.resolver;

import java.util.Map;

import isv.sap.payment.model.IsvPaymentConfigurationModel;

/**
 * This class is intended to lookup payment configuration
 */
public class DefaultPaymentConfigurationResolver
        extends AbstractPaymentConfigurationResolver<IsvPaymentConfigurationModel>
{
    public static final String QUERY_TEMPLATE = "SELECT {pk} FROM {IsvPaymentConfiguration} WHERE {key}=?key";

    @Override
    public String getSearchQuery(final Map<String, Object> params)
    {
        return QUERY_TEMPLATE;
    }
}
