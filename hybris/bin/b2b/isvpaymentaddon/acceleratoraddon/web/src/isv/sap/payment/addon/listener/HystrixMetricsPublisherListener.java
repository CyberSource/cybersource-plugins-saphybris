package isv.sap.payment.addon.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener registers Servo Publisher for Hystrix, it enables metrics to be exposed via JMX.
 */
public class HystrixMetricsPublisherListener implements ServletContextListener
{
    private static final Logger LOG = LoggerFactory.getLogger(HystrixMetricsPublisherListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        try
        {
            doInitializeContext();
        }
        catch (Exception e)
        {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void doInitializeContext()
    {
        try
        {
            registerServoPublisher();
            LOG.info("Servo publisher was registered successfully.");
        }
        catch (IllegalStateException e)
        {
            LOG.warn(e.getMessage(), e);

            HystrixPlugins.reset();
            registerServoPublisher();
        }
    }

    private void registerServoPublisher()
    {
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixServoMetricsPublisher.getInstance());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        // EMPTY
    }
}
