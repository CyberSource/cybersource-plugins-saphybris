package isv.sap.payment.hystrix;

import com.google.common.base.Supplier;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;

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
    protected HystrixCommand<R> create(final Setter setter, final Supplier<R> action)
    {
        return new AbstractTenantAwareHystrixCommand<R>(setter)
        {
            @Override
            protected R runCommand()
            {
                return action.get();
            }
        };
    }
}
