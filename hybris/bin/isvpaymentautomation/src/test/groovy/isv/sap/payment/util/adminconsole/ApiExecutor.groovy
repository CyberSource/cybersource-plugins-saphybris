package isv.sap.payment.util.adminconsole

import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils

/***
 * Creates browserless connection to admin console for a provided server Url.
 * Allows to:
 * Execute an impex request
 * Set a configuration property
 * Get results from a flexible search request
 */
class ApiExecutor
{
    private static final CSRF_REGEX = '<meta name=\"_csrf\" content=\"([^\"]+)'
    private static final LOGIN_URI = 'login'
    private static final LOGIN = 'admin'
    private static final PASSWORD = 'nimda'

    private static final ENCODING = 'UTF-8'
    private final sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
            .build()
    private final cookieStore = new BasicCookieStore()
    private final httpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setSslcontext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setRedirectStrategy(new LaxRedirectStrategy())
            .build()
    private String csrfToken
    String serverUrl

    void startSession(String url)
    {
        goToUrl("$serverUrl/$LOGIN_URI")
        HttpUriRequest loginRequest = RequestBuilder.post()
                .setUri("$serverUrl/j_spring_security_check")
                .addParameter('j_username', LOGIN)
                .addParameter('j_password', PASSWORD)
                .addParameter('submit', 'login')
                .addParameter('_csrf', csrfToken)
                .build()
        CloseableHttpResponse authResponse = httpClient.execute(loginRequest)
        updateCsrf(authResponse)
        authResponse.close()

        goToUrl(url)
    }

    String execute(HttpUriRequest request)
    {
        CloseableHttpResponse response = httpClient.execute(request)
        assert response.statusLine.statusCode == 200
        String responceString = extractResponseBody(response)
        response.close()

        responceString
    }

    String getCsrfToken()
    {
        csrfToken
    }

    private goToUrl(String url)
    {
        HttpGet request = new HttpGet(url)
        CloseableHttpResponse response = httpClient.execute(request)
        updateCsrf(response)
        response.close()
    }

    private updateCsrf(CloseableHttpResponse response)
    {
        String responseString = extractResponseBody(response)
        csrfToken = (responseString =~ /$CSRF_REGEX/)[0][1]
    }

    private String extractResponseBody(CloseableHttpResponse response)
    {
        HttpEntity entity = response.entity
        EntityUtils.toString(entity, ENCODING)
    }
}
