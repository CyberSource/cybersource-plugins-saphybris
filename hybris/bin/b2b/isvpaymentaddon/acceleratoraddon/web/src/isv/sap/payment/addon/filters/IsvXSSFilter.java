package isv.sap.payment.addon.filters;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import de.hybris.platform.servicelayer.web.XSSFilter;
import org.apache.catalina.connector.RequestFacade;

/**
 * Overriding XSSFilter in order to allow excluding some URLs since sometimes isv includes XML elements in their responses
 * (eg. SOP response can contain 'payer_authentication_proof_xml' field)
 */
public class IsvXSSFilter extends XSSFilter
{
    static final String PARAM_EXCLUDED_URLS = "excludedUrls";

    private String[] excludedUrls;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        superInit(filterConfig);

        final String excludedUrlsStr = filterConfig.getInitParameter(PARAM_EXCLUDED_URLS);
        this.excludedUrls = excludedUrlsStr != null ? excludedUrlsStr.split(",") : null;
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        if (isExcludedUrl(servletRequest))
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else
        {
            superDoFilter(servletRequest, servletResponse, filterChain);
        }
    }

    protected void superInit(final FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
    }

    protected void superDoFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException
    {
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    protected boolean isExcludedUrl(final ServletRequest servletRequest)
    {
        final String requestURI = ((RequestFacade) servletRequest).getRequestURI();

        return Arrays.stream(excludedUrls)
                .anyMatch(requestURI::endsWith);
    }
}
