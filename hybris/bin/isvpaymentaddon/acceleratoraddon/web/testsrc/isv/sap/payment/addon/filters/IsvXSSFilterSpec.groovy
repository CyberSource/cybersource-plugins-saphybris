package isv.sap.payment.addon.filters

import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletResponse

import de.hybris.bootstrap.annotations.UnitTest
import org.apache.catalina.connector.RequestFacade
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.addon.filters.IsvXSSFilter.PARAM_EXCLUDED_URLS

@UnitTest
class IsvXSSFilterSpec extends Specification
{
    def filter = Spy(IsvXSSFilter)

    def servletRequest = Mock(RequestFacade)
    def servletResponse = Mock(ServletResponse)
    def filterChain = Mock(FilterChain)
    def filterConfig = Mock(FilterConfig)

    def setup()
    {
        filter.superInit(filterConfig) >> null
        filterConfig.getInitParameter(PARAM_EXCLUDED_URLS) >> '/test/url,/another'
        filter.init(filterConfig)
    }

    @Test
    def 'should skip excluded urls'()
    {
        given:
        servletRequest.requestURI >> url

        when:
        filter.doFilter(servletRequest, servletResponse, filterChain)

        then:
        1 * filter.isExcludedUrl(servletRequest)
        1 * filterChain.doFilter(servletRequest, servletResponse)
        0 * filter.superDoFilter(servletRequest, servletResponse, filterChain)

        where:
        url         | _
        '/test/url' | _
        '/another'  | _
    }

    @Test
    def 'should process not excluded urls'()
    {
        given:
        servletRequest.requestURI >> url

        when:
        filter.doFilter(servletRequest, servletResponse, filterChain)

        then:
        1 * filter.isExcludedUrl(servletRequest)
        1 * filter.superDoFilter(servletRequest, servletResponse, filterChain) >> null

        where:
        url             | _
        '/not/excluded' | _
        '/test/url/2'   | _
        'test'          | _
    }
}
