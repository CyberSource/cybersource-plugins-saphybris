package isv.sap.payment.util.adminconsole

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder

class ImpexRunner
{
    private final static URI = 'console/impex/import'
    private final static RESOURCE_FOLDER = '/impex'
    private final static ON = 'on'
    private final ApiExecutor executor

    ImpexRunner(ApiExecutor executor)
    {
        this.executor = executor
    }

    void fromString(String request)
    {
        run(request)
    }

    void fromTemplate(String fileName, Map map)
    {
        GString impexFile = "$RESOURCE_FOLDER/${fileName}"

        String impex = TemplatePopulator.populateTemplate(impexFile, map)
        run(impex)
    }

    private run(String request)
    {
        String url = executor.serverUrl + URI
        executor.startSession(url)

        HttpUriRequest impexRequest = RequestBuilder.post()
                .setUri(url)
                .addParameter('_csrf', executor.csrfToken)
                .addParameter('scriptContent', request)
                .addParameter('validationEnum', 'IMPORT_STRICT')
                .addParameter('maxThreads', '2')
                .addParameter('encoding', 'UTF-8')
                .addParameter('_legacyMode', ON)
                .addParameter('_enableCodeExecution', ON)
                .addParameter('_distributedMode', ON)
                .addParameter('_sldEnabled', ON)
                .build()

        String response = executor.execute(impexRequest)
        assert response.contains('Import finished successfully')
    }
}
