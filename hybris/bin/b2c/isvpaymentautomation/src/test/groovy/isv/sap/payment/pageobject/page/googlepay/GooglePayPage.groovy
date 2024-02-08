package isv.sap.payment.pageobject.page.googlepay

import geb.Page
import org.openqa.selenium.Keys

import isv.sap.payment.data.Credentials

class GooglePayPage extends Page
{
    static final TIME_PRESET = 'slow'
    static atCheckWaiting = true

    static at = { title.toUpperCase().contains('GOOGLE') }
    static content = {
        email(wait: true) { $('input', type: 'email') }
        password(wait: true) { $('input', type: 'password').not(name: 'hiddenPassword') }
        cardForm(wait: true) { $('#sM432dIframe') }
        pay(wait: true) { $('.b3-primary-button') }
    }

    GooglePayPage loginToGoogle(Credentials googleCredentials)
    {
        email = googleCredentials.email
        email << Keys.ENTER
        password = googleCredentials.password
        password << Keys.ENTER
        browser.at(GooglePayPage)
    }

    void acceptPayment()
    {
        withFrame(cardForm) {
            pay.click()
        }
    }
}
