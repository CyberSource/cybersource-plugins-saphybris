package isv.sap.payment.pageobject.page

import geb.Page

import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.checkout.CheckoutLoginPage
import isv.sap.payment.pageobject.page.visacheckout.VisaCheckoutPage

class CartPage extends Page
{
    static url = 'cart'
    static atCheckWaiting = true
    static at = { $('body.page-cartPage') }
    static content = {
        checkoutCta(to: [B2cCheckoutPage, CheckoutLoginPage, B2bCheckoutPage], wait: true) {
            $('button.btn--continue-checkout', 0)
        }
        visaCheckoutCta(toWait: true, wait: true) { $('img.v-button') }
    }

    CheckoutLoginPage checkoutAsGuest()
    {
        checkoutCta.click(CheckoutLoginPage)
        browser.at(CheckoutLoginPage)
    }

    B2bCheckoutPage checkoutB2B()
    {
        checkoutCta.click(B2bCheckoutPage)
        browser.at(B2bCheckoutPage)
    }

    VisaCheckoutPage startVisaCheckout()
    {
        visaCheckoutCta.click()
        browser.at(VisaCheckoutPage)
    }
}
