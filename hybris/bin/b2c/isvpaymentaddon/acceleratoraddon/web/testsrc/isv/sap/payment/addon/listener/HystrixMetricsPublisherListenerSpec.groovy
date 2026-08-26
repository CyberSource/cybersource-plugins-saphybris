package isv.sap.payment.addon.listener

import jakarta.servlet.ServletContext
import jakarta.servlet.ServletContextEvent
import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class HystrixMetricsPublisherListenerSpec extends Specification
{
    def servletContext = Mock([useObjenesis: false], ServletContext)

    def listener = new HystrixMetricsPublisherListener()

    @Test
    def 'contextInitialized does not throw exception'()
    {
        when:
        listener.contextInitialized(new ServletContextEvent(servletContext))

        then:
        noExceptionThrown()
    }

    @Test
    def 'contextInitialized can be called multiple times safely'()
    {
        when:
        listener.contextInitialized(new ServletContextEvent(servletContext))
        listener.contextInitialized(new ServletContextEvent(servletContext))
        listener.contextInitialized(new ServletContextEvent(servletContext))

        then:
        noExceptionThrown()
    }
}
