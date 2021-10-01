package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

class SelectCountryPage extends Page
{
    static at = { $('#SelectCountryPage') }
    static content = {
        searchBank { $('#BankCodeSearch') }
        searchResults { $('#BankSearcherResultsContent') }
        bic { $('#MultipaysSessionSenderBankCode') }
        next { $('form#WizardForm > div > button') }
        terminate(wait: true) { $('div.secondary-form-actions').find('a.abort') }
    }

    SofortLoginPage inputBic(String bicNumber)
    {
        searchBank = bicNumber
        waitFor { searchResults.displayed }
        next.click()

        browser.at(SofortLoginPage)
    }

    void cancelPayment()
    {
        terminate.click()
        driver.switchTo().alert().accept()
    }
}
