package isv.sap.payment.pageobject.page.alternative.mollie

import geb.Page

class PaymentPage extends Page
{

    static at = {true }
    static content = {
        status { module StatusModule }
        submit { $('button.form__button') }
        expired{$('[class="form__body-tight"] tr:nth-child(3) td code')}
    }

    PaymentPage selectSuccessStatus()
    {
        status.success.click()
        browser.at(PaymentPage)
    }

    PaymentPage selectCancelledStatus()
    {
        status.cancelled.click()
        browser.at(PaymentPage)
    }

    PaymentPage selectFailureStatus()
    {
        status.failure.click()
        browser.at(PaymentPage)
    }

    PaymentPage selectOpenStatus()
    {
        status.open.click()
        browser.at(PaymentPage)
    }

    PaymentPage selectExpiredStatus()
    {
        status.expired.click()
        browser.at(PaymentPage)
    }

     void submitPayment()
    {
        submit.click()
    }

     void expiredPaymentStatus() {
        expired.text().contains("expired")
    }
}
