package isv.sap.payment.configuration.resolver;

import java.util.Map;

/**
 *  Defines resolver component that retrieves configuration based on provided parameters
 */
public interface PaymentConfigurationResolver<T>
{
    /**
     * Resolve configuration data based on input params
     *
     * @param params a map with param values
     * @return configuration value
     */
    T resolve(final Map<String, Object> params);
}
