package isv.sap.payment.pageobject.module.forms

import geb.Module

import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

class GuestLoginForm extends Module
{
    static base = { $('form#guestForm') }
    static content = {
        email { $('input', id: 'guest.email') }
        confirmEmail { $('input', id: 'guest.confirm.email') }
        checkoutAsGuest(to: B2cCheckoutPage) { $('button.guestCheckoutBtn') }
    }
}
