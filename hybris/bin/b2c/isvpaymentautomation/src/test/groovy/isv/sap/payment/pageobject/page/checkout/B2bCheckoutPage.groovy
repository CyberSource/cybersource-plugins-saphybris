package isv.sap.payment.pageobject.page.checkout

import isv.sap.payment.data.TestData
import isv.sap.payment.pageobject.module.B2bPaymentModeModule
import isv.sap.payment.pageobject.module.ReplenishmentModule

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_MONTH
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_YEAR

class B2bCheckoutPage extends AbstractCheckoutPage
{
    static content = {
        paymentMode { module B2bPaymentModeModule }
        paymentTypeCta { $('#choosePaymentType_continue_button') }

        replenishmentCta { $('#scheduleReplenishment', 0) }
        replenishment { module ReplenishmentModule }
    }

    B2bCheckoutPage proceedToAccountPayment()
    {
        paymentMode.account.click()
        paymentTypeCta.click()

        deliveryMethodCta.click()
        browser.at(B2bCheckoutPage)
    }

    B2bCheckoutPage proceedToCardPayment()
    {
        paymentMode.card.click()
        paymentTypeCta.click()

        browser.at(B2bCheckoutPage)
    }

    B2bCheckoutPage proceedToPayPalPayment()
    {
        paymentMode.payPal.click()
        paymentTypeCta.click()

        browser.at(B2bCheckoutPage)
    }

    B2bCheckoutPage populateShippingAndBilling(TestData data)
    {
        shippingForm.with {
            country = data.country
            title = data.title
            firstName = data.firstName
            lastName = data.lastName
            addressLine1 = data.address
            city = data.city
            state = data.region
            postCode = data.postCode
        }

        shippingCta.click()
        deliveryMethodCta.click()

        if (billingTitle.value() != data.title)
        {
            useDeliveryAddress.click()
            waitFor { billingTitle.value() == data.title }
        }
        paymentCta.click()

        browser.at(B2bCheckoutPage)
    }

    B2bCheckoutPage fillSopCard(String type, String number, String cvv)
    {
        card.type = type
        card.number = number
        card.expMonth = EXP_MONTH
        card.expYear = EXP_YEAR
        card.cvv = cvv

        browser.at(B2bCheckoutPage)
    }

    B2bCheckoutPage startReplenishment()
    {
        acceptCheckBox.click()
        replenishmentCta.click()
        browser.at(B2bCheckoutPage)
    }

    void scheduleReplenishment()
    {
        replenishment.calendar.click()
        replenishment.today.click()
        waitFor { !replenishment.today.displayed }
        replenishment.dailySelect.click()
        replenishment.scheduleBtn.click()
    }

    void placeOrder()
    {
        acceptCheckBox.click()
        placeOrderCta.click()
    }
}
