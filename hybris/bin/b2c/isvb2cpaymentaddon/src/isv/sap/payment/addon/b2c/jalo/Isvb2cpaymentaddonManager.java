package isv.sap.payment.addon.b2c.jalo;

import java.util.Map;

import de.hybris.platform.core.Registry;
import de.hybris.platform.util.JspContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.addon.b2c.constants.Isvb2cpaymentaddonConstants;

/**
 * This is the extension manager of the Isvb2cpaymentaddon extension.
 */
public class Isvb2cpaymentaddonManager extends GeneratedIsvb2cpaymentaddonManager
{
    private static final Logger LOG = LoggerFactory.getLogger(Isvb2cpaymentaddonManager.class.getName());

    /**
     * Never call the constructor of any manager directly, call getInstance() You can place your business logic here -
     * like registering a jalo session listener. Each manager is created once for each tenant.
     */
    public Isvb2cpaymentaddonManager()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("constructor of Isvb2cpaymentaddonManager called.");
        }
    }

    /**
     * Get the valid instance of this manager.
     *
     * @return the current instance of this manager
     */
    public static Isvb2cpaymentaddonManager getInstance()
    {
        return (Isvb2cpaymentaddonManager) Registry.getCurrentTenant().getJaloConnection().getExtensionManager()
                .getExtension(
                        Isvb2cpaymentaddonConstants.EXTENSIONNAME);
    }

    /**
     * Use this method to do some basic work only ONCE in the lifetime of a tenant resp. "deployment". This method is
     * called after manager creation (for example within startup of a tenant). Note that if you have more than one tenant
     * you have a manager instance for each tenant.
     */
    @Override
    public void init()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("init() of Isvb2cpaymentaddonManager called. {}", getTenant().getTenantID());
        }
    }

    /**
     * Use this method as a callback when the manager instance is being destroyed (this happens before system
     * initialization, at redeployment or if you shutdown your VM). Note that if you have more than one tenant you have a
     * manager instance for each tenant.
     */
    @Override
    public void destroy()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("destroy() of Isvb2cpaymentaddonManager called, current tenant: {}", getTenant()
                    .getTenantID());
        }
    }

    /**
     * Implement this method to create initial objects. This method will be called by system creator during
     * initialization and system update. Be sure that this method can be called repeatedly.
     *
     * An example usage of this method is to create required cronjobs or modifying the type system (setting e.g some
     * default values)
     *
     * @param params
     *           the parameters provided by user for creation of objects for the extension
     * @param jspc
     *           the jsp context; you can use it to write progress information to the jsp page during creation
     */
    @Override
    public void createEssentialData(final Map<String, String> params, final JspContext jspc)
    {
        // implement here code creating essential data
    }

    /**
     * Implement this method to create data that is used in your project. This method will be called during the system
     * initialization.
     *
     * An example use is to import initial data like currencies or languages for your project from an csv file.
     *
     * @param params
     *           the parameters provided by user for creation of objects for the extension
     * @param jspc
     *           the jsp context; you can use it to write progress information to the jsp page during creation
     */
    @Override
    public void createProjectData(final Map<String, String> params, final JspContext jspc)
    {
        // implement here code creating project data
    }
}
