package isv.sap.payment.pageobject.page.hop

import geb.Page

import isv.sap.payment.pageobject.page.secure3d.Secure3D2PopUpPage

class SaHopPaymentPage extends Page
{
    static atCheckWaiting = true
    static at = { $('#hopRequestForm') }
    static content = {
        paymentFrame(page: PaymentFramePage, wait: true) { $('iframe') }
        reviewFrame(page: ReviewFramePage, wait: true) { $('iframe') }
        blankFrame(wait: true) { $('iframe') }
        subscribeFrame(page: SubscribeFramePage, wait: true) { $('iframe') }
    }

    SaHopPaymentPage authorizePayment(String type, String number, String cvv)
    {
        fillCard(type, number, cvv)
        continuePayment()

        browser.at(SaHopPaymentPage)
    }

    SaHopPaymentPage confirm3DS2Payment()
    {
        confirmPayment()

        browser.at(SaHopPaymentPage)
    }

    void authorizeSubscription(String type, String number, String cvv)
    {
        withFrame(subscribeFrame) {
            (browser.at(SubscribeFramePage)).fillCardData(type, number, cvv)
        }
        withFrame(subscribeFrame) {
            (browser.at(SubscribeFramePage)).continueWithPayment()
        }
    }

    void confirmPayment()
    {
        withFrame(reviewFrame) {
            (browser.at(ReviewFramePage)).clickPayBtn()
        }
    }

    void fill3dSecure2()
    {
        withFrame(blankFrame) {
            (browser.at(Secure3D2PopUpPage)).fill3dSecure2InFrame()
        }
    }

    void cancelPayment()
    {
        withFrame(paymentFrame) {
            (browser.at(PaymentFramePage)).cancelPayment()
        }
    }

    private fillCard(String type, String number, String cvv)
    {
        withFrame(paymentFrame) {
            (browser.at(PaymentFramePage)).fillCardData(type, number, cvv)
        }
    }

    private continuePayment()
    {
        withFrame(paymentFrame) {
            (browser.at(PaymentFramePage)).continueWithPayment()
        }
    }
}
