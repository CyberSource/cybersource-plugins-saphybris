package isv.sap.payment.pageobject.page.alternative.mollie

import geb.Page

class PaymentPage extends Page
{
    static at = { $('.alert--valid').text().contains('iDEAL') }
    static content = {
        status { module StatusModule }
        submit { $('button.form__button') }
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

    void submitPayment()
    {
        submit.click()
    }
}
