package isv.sap.payment.pageobject.page.alternative.klarna

import geb.Page

import isv.sap.payment.data.TestData

class KlarnaAddressFramePage extends Page
{
    static at = { waitFor { js.('document.readyState') == 'complete' } }

    static content = {
        declinedMessage(wait: true) { $('#message-dialog__container') }
        changePaymentMethodButton(wait: true) { $('#message-dialog-button') }
        email(wait: true) { $('#email') }
        postCode(wait: true) { $('#postal_code') }
        userTitle(wait: true) { $('div[data-cid="am.title"] input') }
        firstName(wait: true) { $('#given_name') }
        lastName(wait: true) { $('#family_name') }
        addressLine(wait: true) { $('#street_address') }
        addressLine2(wait: true) { $('#street_address2') }
        city(wait: true) { $('#city') }
        phone(wait: true) { $('#phone') }
        dateOfBirth(wait: true) { $('#date_of_birth') }
        continueBtn(wait: true, required: false) { $('button[clienttoken]>div') }
        editAddressBtn(required: false) { $('a#preview__link') }
        confirmBtn(wait: true) { $('#identification-dialog__footer-button-wrapper button>div') }
    }

    void verifyPaymentIsDeclined()
    {
        declinedMessage.displayed
    }

    void clickChangePaymentMethod()
    {
        changePaymentMethodButton.click()
    }

    void submitBillingAddressForm(TestData data)
    {
        sleep(1000)
        if (editAddressBtn.isDisplayed())
        {
            editAddressBtn.click()
            email = data.email
            dateOfBirth = data.dateOfBirth
            firstName = data.firstName
            lastName = data.lastName
            userTitle = data.title
        }
        else
        {
            email = data.email
            if (postCode.value().toString().isEmpty())
            {
                postCode = data.postCode
            }
            if (addressLine.value().toString().isEmpty())
            {
                addressLine = data.address
            }
            if (city.value().toString().isEmpty())
            {
                city = data.city
            }
            firstName = data.firstName
            lastName = data.lastName
            phone = data.phoneNumber
            dateOfBirth = data.dateOfBirth
            userTitle = data.title
            continueBtn.click()
            sleep(1000)
        }
        continueBtn.click()
        sleep(500)
        confirmBtn.click()
    }
}
