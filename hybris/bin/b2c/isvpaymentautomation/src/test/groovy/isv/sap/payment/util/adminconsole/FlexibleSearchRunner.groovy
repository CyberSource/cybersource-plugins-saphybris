package isv.sap.payment.util.adminconsole

import com.google.gson.Gson
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder

class FlexibleSearchRunner
{
    private static final URI = 'console/flexsearch'
    private static final RESOURCE_FOLDER = '/fsqueries'
    private static final String MAXCOUNT = 1000
    private final ApiExecutor executor

    FlexibleSearchRunner(ApiExecutor executor)
    {
        this.executor = executor
    }

    List<Map> fromString(String request)
    {
        run(request)
    }

    List<Map> fromTemplate(String fileName, Map map)
    {
        GString flexFile = "$RESOURCE_FOLDER/${fileName}"

        String flexRequest = TemplatePopulator.populateTemplate(flexFile, map)
        run(flexRequest)
    }

    private static List<Map> parseResult(String response)
    {
        Map map = new Gson().fromJson(response, LinkedHashMap)
        List<Map> parsedHybrisItems = []

        List<Object> normalizedHeaders = map.headers*.toUpperCase()
        map.resultList.each { row ->
            parsedHybrisItems << [normalizedHeaders, row].transpose().collectEntries { it }
        }
        parsedHybrisItems
    }

    private List<Map> run(String request)
    {
        String url = executor.serverUrl + URI
        executor.startSession(url)

        HttpUriRequest flexRequest = RequestBuilder.post()
                .setUri(url + '/execute')
                .addParameter('flexibleSearchQuery', request)
                .addParameter('_csrf', executor.csrfToken)
                .addParameter('maxCount', MAXCOUNT)
                .build()

        String response = executor.execute(flexRequest)

        parseResult(response)
    }
}
