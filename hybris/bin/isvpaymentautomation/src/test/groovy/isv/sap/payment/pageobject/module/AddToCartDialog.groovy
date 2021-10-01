package isv.sap.payment.pageobject.module

import geb.Module

import isv.sap.payment.pageobject.page.CartPage

class AddToCartDialog extends Module
{
    static base = { $('#colorbox') }
    static content = {
        checoutCta(to: CartPage, wait: true) { $('a.add-to-cart-button') }
    }
}
