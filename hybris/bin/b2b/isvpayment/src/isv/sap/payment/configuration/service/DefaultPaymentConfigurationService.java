package isv.sap.payment.configuration.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import isv.sap.payment.configuration.resolver.PaymentConfigurationResolver;
import isv.sap.payment.enums.IsvConfigurationType;

import static java.util.Collections.emptyMap;

/**
 * Encapsulates the default implementation of {@link PaymentConfigurationService} interface.
 * <p>
 * Implements basic payment configuration retrieval logic through delegation to dedicated
 * {@link PaymentConfigurationResolver} instances.
 */
public class DefaultPaymentConfigurationService implements PaymentConfigurationService
{
    private Map<IsvConfigurationType, PaymentConfigurationResolver> resolverMap = emptyMap();

    @Override
    public <T> T getConfiguration(final IsvConfigurationType type, final Map<String, Object> params)
    {
        final PaymentConfigurationResolver resolver = resolverMap.get(type);

        Assert.notNull(resolver, "No resolver defined for configuration type.");

        return (T) resolver.resolve(params);
    }

    @Required
    public void setResolverMap(final Map<IsvConfigurationType, PaymentConfigurationResolver> resolverMap)
    {
        this.resolverMap = resolverMap;
    }
}
