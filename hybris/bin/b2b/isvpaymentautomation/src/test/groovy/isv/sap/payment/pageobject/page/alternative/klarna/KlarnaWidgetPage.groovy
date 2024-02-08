package isv.sap.payment.pageobject.page.alternative.klarna

import geb.Page

import isv.sap.payment.data.TestData

class KlarnaWidgetPage extends Page
{
    static final TIME_PRESET = 'slow'

    static at = { klarnaFrame }
    static content = {
        klarnaFrame(page: KlarnaAddressFramePage, wait: TIME_PRESET) { $('iframe', id: 'klarna-credit-fullscreen') }
    }

    void verifyDeniedMessageDisplayed()
    {
        withFrame(klarnaFrame) {
            (browser.at(KlarnaAddressFramePage)).verifyPaymentIsDeclined()
            (browser.page as KlarnaAddressFramePage).clickChangePaymentMethod()
        }
    }

    void changePaymentMethod()
    {
        withFrame(klarnaFrame) {
            (browser.at(KlarnaAddressFramePage))
                    .clickChangePaymentMethod()
        }
    }

    void submitBillingAddressForm(TestData data)
    {
        withFrame(klarnaFrame) {
            (browser.at(KlarnaAddressFramePage))
                    .submitBillingAddressForm(data)
        }
    }
}
