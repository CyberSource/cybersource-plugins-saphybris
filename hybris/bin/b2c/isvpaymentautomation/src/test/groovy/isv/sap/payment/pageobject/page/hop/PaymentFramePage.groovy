package isv.sap.payment.pageobject.page.hop

import geb.Page

import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ReplenishmentConfirmationPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_MONTH
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_YEAR

class PaymentFramePage extends Page
{
    static atCheckWaiting = true
    static at = { $('#payment') }
    static content = {
        cardType { type -> $(id: type) }
        cardNumber { $('input#card_number') }
        cardCvn(wait: true) { $('input#card_cvn') }
        expirationMonth { $('select#card_expiry_month') }
        expirationYear { $('select#card_expiry_year') }

        nextBtn(wait: true) { $('input.right') }
        finishBtn(to: [OrderConfirmationPage, ReplenishmentConfirmationPage]) { nextBtn }
        completeBtn(wait: true) { $('input.complete') }

        cancelBtn { $('button.cancelbutton') }
        confirmBtn(wait: true, to: B2cCheckoutPage) { $('button', text: 'Yes') }
    }

    void fillCardData(String type, String number, String cvv)
    {
        cardType(type).click()
        cardNumber = number
        cardCvn = cvv
        expirationMonth = EXP_MONTH
        expirationYear = EXP_YEAR
    }

    void cancelPayment()
    {
        cancelBtn.click()
        confirmBtn.click()
    }

    void continueWithPayment()
    {
        nextBtn.click()
    }
}
