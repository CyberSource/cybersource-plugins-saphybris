package isv.sap.payment.util.adminconsole

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder

class ConfigPropertiesSetter
{
    private static final ACCESS_URI = 'platform/config'
    private static final PLACE_URI = 'platform/configstore'
    private final ApiExecutor executor

    ConfigPropertiesSetter(ApiExecutor executor)
    {
        this.executor = executor
    }

    void setHybrisProperty(String key, String value)
    {
        String url = executor.serverUrl + ACCESS_URI
        executor.startSession(url)

        HttpUriRequest request = RequestBuilder.post()
                .setUri(executor.serverUrl + PLACE_URI)
                .addParameter('key', key)
                .addParameter('val', value)
                .addParameter('_csrf', executor.csrfToken)
                .build()

        executor.execute(request)
    }
}
