package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

import isv.sap.payment.pageobject.page.OrderConfirmationPage

class ProvideTanPage extends Page
{
    static at = { $('#ProvideTanPage') }
    static content = {
        tan { $('#BackendFormTan') }
        next { $('form#WizardForm > button') }
    }

    OrderConfirmationPage provideTan(String tanNum)
    {
        tan = tanNum
        next.click(OrderConfirmationPage)

        browser.at(OrderConfirmationPage)
    }
}
