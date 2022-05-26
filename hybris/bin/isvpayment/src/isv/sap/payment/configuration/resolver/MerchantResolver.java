package isv.sap.payment.configuration.resolver;

import java.util.Map;

import isv.sap.payment.model.IsvMerchantModel;

/**
 * This class is responsible for retrieving merchant configuration
 */
public class MerchantResolver extends AbstractPaymentConfigurationResolver<IsvMerchantModel>
{
    private static final String FIND_MERCHANT_BY_ID =
            "SELECT {" + IsvMerchantModel.PK + "} FROM {" + IsvMerchantModel._TYPECODE + "} WHERE {id} = ?id";

    @Override
    public String getSearchQuery(final Map<String, Object> params)
    {
        return FIND_MERCHANT_BY_ID;
    }
}
