package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

class SelectCountryPage extends Page
{
    static at = { $('#SelectCountryPage') }
    static content = {
        searchBank { $('#BankCodeSearch') }
        searchResults { $('#BankSearcherResultsContent') }
        bic { $('#MultipaysSessionSenderBankCode') }
        selectCookiesBtn(required: false) { $('div#Modal #cookie-modal-basic button.cookie-modal-settings') }
        saveCookiesBtn(required: false) { $('#Modal #cookie-modal-extended button.cookie-modal-save') }
        next { $('form#WizardForm > button') }
        terminate(wait: true) { $('div.secondary-form-actions').find('a.abort') }
    }

    void acceptCookies()
    {
        if (selectCookiesBtn.isDisplayed())
        {
            selectCookiesBtn.click()
            saveCookiesBtn.click()
        }
    }

    SofortLoginPage inputBic(String bicNumber)
    {
        acceptCookies()
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
