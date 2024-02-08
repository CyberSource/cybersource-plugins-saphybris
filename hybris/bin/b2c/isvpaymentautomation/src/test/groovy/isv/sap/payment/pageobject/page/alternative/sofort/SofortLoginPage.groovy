package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

class SofortLoginPage extends Page
{
    static at = { $('#LoginPage') }
    static content = {
        account(wait: true) { $('#BackendFormLOGINNAMEUSERID') }
        pin { $('#BackendFormUSERPIN') }
        next { $('form#WizardForm > button') }
    }

    SelectAccountPage loginToSofort(String login, String password)
    {
        account = login
        pin = password
        next.click()

        browser.at(SelectAccountPage)
    }
}
