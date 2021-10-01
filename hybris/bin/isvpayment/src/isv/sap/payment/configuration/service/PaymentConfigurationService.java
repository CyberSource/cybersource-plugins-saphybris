package isv.sap.payment.configuration.service;

import java.util.Map;

import isv.sap.payment.enums.IsvConfigurationType;

/**
 * This interface defines operations on payment configuration.
 */
public interface PaymentConfigurationService
{
    /**
     * Find configuration instance based by type
     *
     * @param configType configuration type
     * @param params required params in order to identify appropriate configuration instance
     * @param <T> configuration value type
     * @return configuration value
     */
    <T> T getConfiguration(final IsvConfigurationType configType, final Map<String, Object> params);
}
