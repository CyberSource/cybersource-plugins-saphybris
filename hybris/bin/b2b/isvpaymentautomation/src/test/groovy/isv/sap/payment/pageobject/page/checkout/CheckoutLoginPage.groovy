package isv.sap.payment.pageobject.page.checkout

import geb.Page

import isv.sap.payment.pageobject.module.forms.GuestLoginForm

class CheckoutLoginPage extends Page
{
    static at = { $('body.page-checkout-login') }
    static content = {
        guestLogin { module GuestLoginForm }
    }

    B2cCheckoutPage loginAsGuest(String mail)
    {
        guestLogin.email = mail
        guestLogin.confirmEmail = mail
        guestLogin.checkoutAsGuest.click()

        browser.at(B2cCheckoutPage)
    }
}
