package isv.sap.payment.pageobject.module

import geb.Module

import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage

class B2cPaymentModeModule extends Module
{
    static base = { $('div.checkout-paymentmethod') }
    static content = {
        creditCard { $('#paymentMode_1_creditcard') }
        payPal { $('#paymentMode_2_paypal') }
        ideal { $('#paymentMode_3_ideal') }
        sofort { $('#paymentMode_4_sofort') }
        bancontact { $('#paymentMode_5_bancontact') }
        aliPay { $('#paymentMode_6_alipay') }
        visaCheckout { $('#paymentMode_7_visacheckout') }
        klarna { $('#paymentMode_8_klarna') }
        klarnaFrame { $('#klarna-credit-main') }
        googlePay { $('#paymentMode_10_googlepay') }
    }

    B2cCheckoutPage selectCreditCard()
    {
        sleep(700)

        creditCard.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectPayPal()
    {
        payPal.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectIdeal()
    {
        ideal.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectSofort()
    {
        sofort.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectBancontact()
    {
        bancontact.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectAliPay()
    {
        aliPay.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectVisaCheckout()
    {
        visaCheckout.click()

        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectKlarna()
    {
        klarna.click()
        waitFor { klarnaFrame }
        sleep(5000)
        browser.at(B2cCheckoutPage)
    }

    B2cCheckoutPage selectGooglePay()
    {
        googlePay.click()
        browser.at(B2cCheckoutPage)
    }
}
