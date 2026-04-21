package isv.sap.payment.configuration.resolver;

import de.hybris.platform.core.Registry;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import isv.cjl.payment.configuration.resolver.ConfigurationResolver;

public class IsvConfigurationResolver implements ConfigurationResolver
{
    @Override
    public Configuration resolve()
    {
        //return new MapConfiguration(Registry.getMasterTenant().getConfig().getAllParameters());
        final Map<String, Object> params = new HashMap<>(Registry.getMasterTenant().getConfig().getAllParameters());
        return new MapConfiguration(params);
    }
}
