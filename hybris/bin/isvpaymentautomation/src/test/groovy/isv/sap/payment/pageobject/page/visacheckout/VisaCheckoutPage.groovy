package isv.sap.payment.pageobject.page.visacheckout

import geb.Page

import isv.sap.payment.data.TestData

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.CVV
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_DATE
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.VISA

class VisaCheckoutPage extends Page
{
    static at = { '.header-img-visaLogo.header-logo' }
    static atCheckWaiting = true

    static content = {
        vcoFrame(wait: true) { $('.vcop-src-frame') }
        threedsframe(wait: true, required: false) { $('.threedsframe') }

        newBtn(wait: true, required: false) { $('#tabs__tab-id__0') }
        cancelBtn(wait: true) { $('button.close', 0) }
        notyouBtn(wait: true, required: false) { $('button.secondary-cta', 0) }
        continueAsGuest(wait: true, required: false) { $('button', value: 'Continue as guest') }

        // Card input fields
        cardNumber(wait: true) { $('input#cardNumber-CC') }
        expDate { $('input#exp-date') }
        cvv { $('input#cvv') }
        continueBtn(wait: true) { $('button', name: 'btnContinue', 0) }

        // Billing Address input fields
        firstName(wait: true) { $('input#firstName') }
        lastName { $('input#lastName') }
        countryCombobox { $('input#countryCombobox') }
        ukCountry { $('li', 'data-shortvalue': 'GB', 0) }
        addressLine1 { $('input#line1') }
        city { $('input#city') }
        postalCode { $('input#postalCode') }
        phoneDialCode { $('span.dial-code-selector', 0) }
        ukDialCode { $('li', 'data-shortvalue': 'GB', 1) }
        phoneNumber { $('input#phone-number-field') }
        email { $('input#email') }

        // 3ds password
        password(wait: true) { $('input#password') }
        submitBtn { $('input', name: 'UsernamePasswordEntry') }
    }

    void performVisaCheckout(TestData data)
    {
        waitFor { vcoFrame }
        sleep(2000)

        withFrame(vcoFrame) {
            if (notyouBtn)
            {
                notyouBtn.click()
            }

            if (newBtn)
            {
                newBtn.click()

                fillCardDetails()

                if (continueAsGuest)
                {
                    continueAsGuest.click()
                }
                else
                {
                    fillBillingAddressForm(data)
                }
            }

            waitFor { continueBtn.click() }

            fill3dsForm()
        }
    }

    VisaCheckoutPage fillCardDetails()
    {
        cardNumber = VISA
        expDate = EXP_DATE
        cvv = CVV
        continueBtn.click()

        browser.at(VisaCheckoutPage)
    }

    VisaCheckoutPage fillBillingAddressForm(TestData data)
    {
        firstName << data.firstName
        lastName << data.lastName
        countryCombobox.click()
        ukCountry.click()
        addressLine1 << data.address
        city << data.city
        postalCode << data.postCode
        phoneDialCode.click()
        ukDialCode.click()
        phoneNumber << data.phoneNumber
        email << data.email

        continueBtn.click()

        browser.at(VisaCheckoutPage)
    }

    VisaCheckoutPage fill3dsForm()
    {
        if (threedsframe)
        {
            withFrame(threedsframe) {
                password << '1234'
                submitBtn.click()
            }
        }
        browser.at(VisaCheckoutPage)
    }

    VisaCheckoutPage cancelVisaCheckout()
    {
        waitFor(30, 1) { vcoFrame }
        sleep(2000)

        withFrame(vcoFrame) {
            cancelBtn.click()
        }

        browser.at(VisaCheckoutPage)
    }
}
