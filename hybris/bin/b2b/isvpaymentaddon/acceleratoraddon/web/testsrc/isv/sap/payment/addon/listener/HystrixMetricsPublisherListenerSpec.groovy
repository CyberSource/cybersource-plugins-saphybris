package isv.sap.payment.addon.listener

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent

import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher
import com.netflix.hystrix.strategy.HystrixPlugins
import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class HystrixMetricsPublisherListenerSpec extends Specification
{
    def servletContext = Mock([useObjenesis: false], ServletContext)

    def listener = new HystrixMetricsPublisherListener()

    @Test
    def 'ContextInitialized servo metrics publisher registered'()
    {
        when:
        listener.contextInitialized(new ServletContextEvent(servletContext))

        then:
        HystrixServoMetricsPublisher == HystrixPlugins.instance.metricsPublisher.getClass()
    }

    @Test
    def 'register when ContextInitialized is called several times'()
    {
        when:
        listener.contextInitialized(new ServletContextEvent(servletContext))
        listener.contextInitialized(new ServletContextEvent(servletContext))
        listener.contextInitialized(new ServletContextEvent(servletContext))

        then:
        HystrixServoMetricsPublisher == HystrixPlugins.instance.metricsPublisher.getClass()
    }
}
