package isv.sap.payment.addon.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy listener kept for compatibility after Hystrix removal.
 */
public class HystrixMetricsPublisherListener implements ServletContextListener
{
    private static final Logger LOG = LoggerFactory.getLogger(HystrixMetricsPublisherListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        LOG.info("Hystrix metrics publisher listener is disabled after migration to Resilience4j.");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        // EMPTY
    }
}
