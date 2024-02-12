package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

class SelectAccountPage extends Page
{
    static at = { $('#SelectAccountPage') }
    static content = {
        firstAccount { $('#account-1') }
        next { $('form#WizardForm > button') }
    }

    ProvideTanPage selectAccount()
    {
        firstAccount.click()
        next.click(ProvideTanPage)

        browser.at(ProvideTanPage)
    }
}
