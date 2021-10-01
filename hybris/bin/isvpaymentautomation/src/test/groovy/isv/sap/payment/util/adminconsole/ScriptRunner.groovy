package isv.sap.payment.util.adminconsole

import com.google.gson.Gson
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder

class ScriptRunner
{
    private static final URI = 'console/scripting'
    private static final RESOURCE_FOLDER = '/scripts'
    private final ApiExecutor executor

    ScriptRunner(ApiExecutor executor)
    {
        this.executor = executor
    }

    String fromString(String request)
    {
        run(request)
    }

    String fromTemplate(String fileName, Map map)
    {
        GString scriptFile = "$RESOURCE_FOLDER/${fileName}"

        String script = TemplatePopulator.populateTemplate(scriptFile, map)
        run(script)
    }

    private static String parseResult(String response)
    {
        Map result = new Gson().fromJson(response, LinkedHashMap)

        result['executionResult']
    }

    private String run(String script)
    {
        String url = executor.serverUrl + URI
        executor.startSession(url)

        HttpUriRequest scriptRequest = RequestBuilder.post()
                .setUri(url + '/execute')
                .addParameter('_csrf', executor.csrfToken)
                .addParameter('scriptType', 'groovy')
                .addParameter('script', script)
                .addParameter('commit', 'true')
                .build()

        String response = executor.execute(scriptRequest)

        parseResult(response)
    }
}
