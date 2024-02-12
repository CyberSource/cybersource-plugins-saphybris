package isv.sap.payment.pageobject.page.alternative.sofort

import geb.Page

import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

class AbortPage extends Page
{
    static at = { $('#AbortPage') }
    static content = {
        back(to: B2cCheckoutPage, wait: true) { $('button.back-to-shop') }
    }

    B2cCheckoutPage returnToChecout()
    {
        back.click()

        browser.at(B2cCheckoutPage)
    }
}
