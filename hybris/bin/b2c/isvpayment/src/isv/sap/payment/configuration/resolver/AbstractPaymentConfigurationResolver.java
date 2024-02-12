package isv.sap.payment.configuration.resolver;

import java.util.List;
import java.util.Map;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.util.Config;
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import static isv.sap.payment.utils.Assert.isTrue;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Base implementation for configuration lookup
 *
 * @param <T> configuration value type
 */
public abstract class AbstractPaymentConfigurationResolver<T> implements PaymentConfigurationResolver<T>
{
    private FlexibleSearchService flexibleSearchService;

    public abstract String getSearchQuery(final Map<String, Object> params);

    @Override
    public T resolve(final Map<String, Object> params)
    {
        final FlexibleSearchQuery query = new FlexibleSearchQuery(getSearchQuery(params), params);
        final List<T> result = flexibleSearchService.<T>search(query).getResult();
        isTrue(isNotEmpty(result), () -> new ModelNotFoundException("No result for the given query."));
        isTrue(result.size() == 1, () -> new AmbiguousIdentifierException("Multiple results found for given query."));
       
        return result.get(0);
    }

    public FlexibleSearchService getFlexibleSearchService()
    {
        return flexibleSearchService;
    }

    @Required
    public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
    {
        this.flexibleSearchService = flexibleSearchService;
    }
}
