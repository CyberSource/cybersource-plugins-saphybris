package isv.sap.payment.pageobject.page.alternative.mollie

import geb.Page

import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

class SelectBankPage extends Page
{
    static atCheckWaiting = true
    static at = { title.contains('ISV powered') }
    static content = {
        ingBank(to: PaymentPage) { $('button.grid-button-ideal-INGBNL2A') }
        cancel(to: B2cCheckoutPage) { $('#cancel-button').children('button') }
    }

    PaymentPage selectINGBank()
    {
        ingBank.click()
        browser.at(PaymentPage)
    }
}
