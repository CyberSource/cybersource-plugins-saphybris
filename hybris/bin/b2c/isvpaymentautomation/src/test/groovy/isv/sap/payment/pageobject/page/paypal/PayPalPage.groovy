package isv.sap.payment.pageobject.page.paypal

import geb.Page
import org.openqa.selenium.Keys

import isv.sap.payment.data.Credentials
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

class PayPalPage extends Page
{
    static final TIME_PRESET = 'slow'
    static atCheckWaiting = true

    static at = { title.contains('Log in') || title.contains('PayPal') }
    static content = {
        acceptCookies(wait: true, required: false) { $('button#acceptAllButton') }

        cartAmount(wait: 5, required: false) { $('button.Cart_cartWrapper_3iK3_') }
        shippingDetails(wait: true, required: false) { $('.SingleShippingAddress_name_3siDP') }
        guestForm(wait: 5, required: false) { $('#singlePagePaymentForm') }

        guestLogin(wait: 5) { $('div.baslLoginButtonContainer') }

        spinner(required: false) { $('#spinner') }

        loginSection(required: false) { $('section#login') }

        email(wait: true) { $('#email') }
        nextBtn(required: false) { $('#btnNext') }
        password(wait: true) { $('#password') }
        logInBtn { $('#btnLogin') }

        cancel(wait: true) { $('a.CancelLink_cancel-link_2uud4') }
        continueBtn(required: false, wait: true) { $('#payment-submit-btn') }
        selectPaymentContinueBtn(required: false, wait: true) { $('button.continueButton') }
    }

    PayPalPage loginToPayPal(Credentials payPalCredentials)
    {
        // PayPal has many different login interfaces, which needs to be managed
        waitFor { !spinner.displayed }

        if (acceptCookies)
        {
            acceptCookies.click()
        }

        // if guest form is open
        if (guestForm)
        {
            guestLogin.click()
        }

        // if user is already logged in
        if (!cartAmount)
        {
            waitFor { loginSection }

            email = payPalCredentials.email
            if (nextBtn)
            {
                nextBtn.click()
            } // for cases where email and password are separated

            waitFor { password.click() }
            password = payPalCredentials.password

            logInBtn.click()
        }

        browser.at(PayPalPage)
    }

    OrderConfirmationPage acceptPayment()
    {
        waitFor { shippingDetails.displayed }
        continueBtn << Keys.RETURN

        browser.at(OrderConfirmationPage)
    }

    B2cCheckoutPage cancelPayment()
    {
        cancel << Keys.RETURN

        browser.at(B2cCheckoutPage)
    }
}
