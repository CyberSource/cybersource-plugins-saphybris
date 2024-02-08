package isv.sap.payment.sampledata.jalo;

import java.util.Map;

import de.hybris.platform.core.Registry;
import de.hybris.platform.util.JspContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.sampledata.constants.IsvpaymentsampledataConstants;

public class IsvpaymentsampledataManager extends GeneratedIsvpaymentsampledataManager
{
    private static final Logger LOG = LoggerFactory.getLogger(IsvpaymentsampledataManager.class);

    public IsvpaymentsampledataManager()
    {
        LOG.debug("constructor of IsvpaymentsampledataManager called.");
    }

    public static IsvpaymentsampledataManager getInstance()
    {
        return (IsvpaymentsampledataManager) Registry.getCurrentTenant().getJaloConnection()
                .getExtensionManager()
                .getExtension(IsvpaymentsampledataConstants.EXTENSIONNAME);
    }

    @Override
    public void init()
    {
        LOG.debug("init() of IsvpaymentsampledataManager called, current tenant: {}",
                getTenant().getTenantID());
    }

    @Override
    public void destroy()
    {
        LOG.debug("destroy() of IsvpaymentsampledataManager called, current tenant: {}",
                getTenant().getTenantID());
    }

    @Override
    public void createEssentialData(final Map<String, String> params, final JspContext jspc)
    {
        // implement here code creating essential data
    }

    @Override
    public void createProjectData(final Map<String, String> params, final JspContext jspc)
    {
        // implement here code creating project data
    }
}
