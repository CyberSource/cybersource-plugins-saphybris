package isv.sap.payment.addon.utils;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import isv.cjl.payment.exception.PaymentException;

public final class HttpRequestUtil
{
    // The limit on number of parameters to accept was requested by ISV security team,
    // although there is tomcat maxPostSize configured which should take care of this.
    // Authorization response is around 80, so with additional services enabled it should cover all scenarios
    private static final int MAX_PARAMETERS = 300;

    private HttpRequestUtil()
    {
        // EMPTY
    }

    public static Map<String, String> getParametersMap(final HttpServletRequest request)
    {
        final Map<String, String[]> params = request.getParameterMap();

        // Protection against Heap exhaustive requests
        if (MAX_PARAMETERS < params.size())
        {
            throw new PaymentException("Received HTTP request with large parameters map: " + params.size());
        }

        final Map<String, String> parameterMap = new HashMap<>(params.size());

        for (final Map.Entry<String, String[]> entry : params.entrySet())
        {
            final String[] value = entry.getValue();
            parameterMap.put(entry.getKey(), value[0]);
        }

        return parameterMap;
    }
}
