package isv.sap.payment.hystrix;

import com.netflix.hystrix.HystrixCommand;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;

/**
 * Abstract Hystrix command to activate the tenant in the current thread before execution.
 */
public abstract class AbstractTenantAwareHystrixCommand<R> extends HystrixCommand<R>
{
    private final Tenant tenant;

    protected AbstractTenantAwareHystrixCommand(final Setter setter)
    {
        super(setter);
        this.tenant = Registry.getCurrentTenantNoFallback();
    }

    @Override
    protected R run() throws Exception // NOPMD
    {
        if (Registry.hasCurrentTenant() && Registry.isCurrentTenant(tenant))
        {
            return runCommand();
        }

        Registry.setCurrentTenant(tenant);

        try
        {
            return runCommand();
        }
        finally
        {
            Registry.unsetCurrentTenant();
        }
    }

    /**
     * Implement this method with code to be executed when {@link #execute()} or {@link #queue()} are invoked.
     *
     * @return R command execution result
     */
    protected abstract R runCommand();
}
