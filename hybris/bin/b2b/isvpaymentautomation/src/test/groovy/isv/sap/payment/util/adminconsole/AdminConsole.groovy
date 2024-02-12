package isv.sap.payment.util.adminconsole

class AdminConsole
{
    private final ApiExecutor executor

    AdminConsole(String baseUrl)
    {
        executor = new ApiExecutor(serverUrl: baseUrl)
    }

    ImpexRunner runImpex()
    {
        new ImpexRunner(executor)
    }

    FlexibleSearchRunner runFlexibleSearch()
    {
        new FlexibleSearchRunner(executor)
    }

    ScriptRunner runScript()
    {
        new ScriptRunner(executor)
    }

    ConfigPropertiesSetter updateConfig()
    {
        new ConfigPropertiesSetter(executor)
    }
}
