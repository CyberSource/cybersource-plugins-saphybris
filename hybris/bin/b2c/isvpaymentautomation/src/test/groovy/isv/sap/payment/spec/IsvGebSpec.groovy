package isv.sap.payment.spec

import geb.spock.GebReportingSpec
import spock.lang.Retry
import spock.lang.Shared

import isv.sap.payment.data.TestData
import isv.sap.payment.util.WithTestData
import isv.sap.payment.util.adminconsole.AdminConsole
import isv.sap.payment.util.api.AdminApi

@Retry(count = 1, condition = { System.properties.retry.toString().toBoolean() })
class IsvGebSpec extends GebReportingSpec implements WithTestData
{
    @Shared
    protected api = new AdminApi(new AdminConsole(browser.config.rawConfig.hac.toString()))

    private final gebConfig = browser.config.rawConfig
    protected TestData data

    void useUkSite()
    {
        browser.config.baseUrl = gebConfig.ukSite
        data = getData('uk')
    }

    void useDeSite()
    {
        browser.config.baseUrl = gebConfig.deSite
        data = getData('de')
    }

    void useB2bSite()
    {
        browser.config.baseUrl = gebConfig.b2bSite
        data = getData('b2b')
    }
}
