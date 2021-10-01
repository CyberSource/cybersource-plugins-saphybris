package isv.sap.payment.pageobject.page

import geb.Page

class OrderConfirmationPage extends Page
{
    static atCheckWaiting = true
    static at = { $('body.page-orderConfirmationPage') }
    static content = {
        orderNumber(wait: true) { $('span.item-value').first() }
        shippingAddress(wait: true) { $('.value-order').first() }
    }

    String extractOrderNumber()
    {
        orderNumber.text()
    }

    String extractShippingAddress()
    {
        shippingAddress.text()
    }
}
