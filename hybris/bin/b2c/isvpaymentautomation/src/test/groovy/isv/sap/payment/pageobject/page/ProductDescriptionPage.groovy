package isv.sap.payment.pageobject.page

import geb.Page
import org.openqa.selenium.By
import org.openqa.selenium.Keys

import isv.sap.payment.pageobject.module.AddToCartDialog

class ProductDescriptionPage extends Page
{
    static atCheckWaiting = true
    static url = 'search/?text='
    static at = {$('button.js-enable-btn', 0)}
   static content = {
        qtyInput { $('input.js-qty-selector-input') }
        selectProduct(wait: true){$('button.js-enable-btn', 0)}
        addToCartCta(wait: true) { $('#addToCartButton').click() }
        addToCartDialog { module AddToCartDialog }
    }

    CartPage addProductToCart()
    {
        selectProduct.click()
        addToCartDialog.checoutCta.click()

        browser.at(CartPage)
    }
}
