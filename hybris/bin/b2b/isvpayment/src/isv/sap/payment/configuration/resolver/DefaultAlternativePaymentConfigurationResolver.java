package isv.sap.payment.configuration.resolver;

import java.util.Map;

import isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel;

/**
 * This class is intended to lookup alternative payment configuration
 */
public class DefaultAlternativePaymentConfigurationResolver
        extends AbstractPaymentConfigurationResolver<IsvCheckAlternativePaymentStatusConfigurationModel>
{
    private static final String QUERY_TEMPLATE =
            "SELECT {" + IsvCheckAlternativePaymentStatusConfigurationModel.PK + "} FROM "
                    + "{" + IsvCheckAlternativePaymentStatusConfigurationModel._TYPECODE
                    + "} WHERE {" + IsvCheckAlternativePaymentStatusConfigurationModel.PAYMENTMETHOD
                    + "}=?paymentMethod";

    @Override
    public String getSearchQuery(final Map<String, Object> params)
    {
        return QUERY_TEMPLATE;
    }
}
