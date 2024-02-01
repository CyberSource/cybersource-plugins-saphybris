package isv.sap.payment.pageobject.page

import geb.Page

import isv.sap.payment.pageobject.module.AddToCartDialog

class ProductDescriptionPage extends Page
{
    static atCheckWaiting = true
    static url = 'p'
    static at = { $('body.page-productDetails') }
    static content = {
        qtyInput { $('input.js-qty-selector-input') }
        addToCartCta(wait: true) { $('#addToCartButton') }
        addToCartDialog { module AddToCartDialog }
    }

    CartPage addProductToCart()
    {
        addToCartCta.click()
        addToCartDialog.checoutCta.click()

        browser.at(CartPage)
    }
}
