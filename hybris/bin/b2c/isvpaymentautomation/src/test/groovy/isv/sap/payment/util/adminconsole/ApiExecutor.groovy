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
class ApiExecutor {
    private static final CSRF_REGEX = "name=\"_csrf\"\\s+value=\"([^\"]+)\"";
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
    public String csrfToken
   public String serverUrl

    /**
     * Starts a session by logging in to the admin console and retrieving the CSRF token.
     * @param url The URL to navigate to after login
     */
    void startSession(String url) {
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

    /**
     * Executes the given HTTP request.
     * @param request The HTTP request to execute
     * @return The response body as a string
     */
    String execute(HttpUriRequest request) {
        CloseableHttpResponse response = httpClient.execute(request)
        assert response.statusLine.statusCode == 200
        String responseString = extractResponseBody(response)
        response.close()
        return responseString
    }

    /**
     * Gets the CSRF token from the current session.
     * @return The CSRF token
     */
    String getCsrfToken() {
        return csrfToken
    }

    /**
     * Helper method to send a GET request to the provided URL and update the CSRF token.
     * @param url The URL to send the GET request to
     */
    private goToUrl(String url) {
        HttpGet request = new HttpGet(url)

        CloseableHttpResponse response = httpClient.execute(request)
        updateCsrf(response)
        response.close()
    }

    /**
     * Updates the CSRF token by parsing the response body.
     * @param response The HTTP response from which the CSRF token is extracted
     */

    private updateCsrf(CloseableHttpResponse response) {
        String responseString = extractResponseBody(response)
        csrfToken = (responseString =~ /${CSRF_REGEX}/)[0][1]
    }

    /**
     * Extracts the response body from the given HTTP response.
     * @param response The HTTP response
     * @return The response body as a string
     */
    private String extractResponseBody(CloseableHttpResponse response) {
        HttpEntity entity = response.entity
        return EntityUtils.toString(entity, ENCODING)
    }

    /**
     * Validates the server URL to ensure it starts with 'http://' or 'https://'
     * @param url The server URL
     * @return true if the URL is valid, false otherwise
     */
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
