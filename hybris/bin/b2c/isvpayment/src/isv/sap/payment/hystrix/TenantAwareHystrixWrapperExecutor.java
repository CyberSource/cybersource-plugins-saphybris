package isv.sap.payment.hystrix;

import com.google.common.base.Supplier;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;

import isv.cjl.payment.hystrix.HystrixWrapperExecutor;

/**
 * Executes all actions in the Hystrix context, using circuit-breaker pattern provided by library
 *
 * @param <R> result type
 */
public class TenantAwareHystrixWrapperExecutor<R> extends HystrixWrapperExecutor<R>
{
    public TenantAwareHystrixWrapperExecutor(final String isvPaymentGroup)
    {
        super(isvPaymentGroup);
    }

    @Override
    public R execute(final String commandKey, final Supplier<R> action)
    {
        final Tenant tenant = Registry.getCurrentTenantNoFallback();
        return super.execute(commandKey, () ->
        {
            if (Registry.hasCurrentTenant() && Registry.isCurrentTenant(tenant))
            {
                return action.get();
            }

            Registry.setCurrentTenant(tenant);
            try
            {
                return action.get();
            }
            finally
            {
                Registry.unsetCurrentTenant();
            }
        });
    }
}
