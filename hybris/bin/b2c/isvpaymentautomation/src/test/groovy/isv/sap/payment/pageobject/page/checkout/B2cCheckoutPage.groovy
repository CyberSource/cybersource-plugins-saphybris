package isv.sap.payment.pageobject.page.checkout

import isv.sap.payment.data.TestData
import isv.sap.payment.pageobject.module.B2cPaymentModeModule
import isv.sap.payment.pageobject.page.secure3d.Secure3D2PopUpPage

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_MONTH
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.EXP_YEAR

class B2cCheckoutPage extends AbstractCheckoutPage
{
    static content = {
        paymentMode { module B2cPaymentModeModule }
        sopRequestFrame(page: Secure3D2PopUpPage, wait: true) { $('iframe#sopRequestIframe') }
        firstPaymentMethodLogo { $('div.payment-logos', 0) }
    }

    B2cCheckoutPage startPayment()
    {
        waitFor { billingTitle.value() != null }
        paymentCta.click()
        waitFor { firstPaymentMethodLogo.displayed }
        return this
    }

    B2cCheckoutPage startWithPayment()
    {
        try {
            waitFor(10, false) { billingTitle.value() != null }
        } catch (ignored) {
            useDeliveryAddress.click()
        }
        paymentCta.click()
        waitFor { firstPaymentMethodLogo.displayed }
        return this
    }

    B2cCheckoutPage populateShippingAndBilling(TestData data)
    {
        shippingForm.country = data.country
        shippingForm.title = data.title
        shippingForm.firstName = data.firstName
        shippingForm.lastName = data.lastName
        shippingForm.addressLine1 = data.address
        shippingForm.city = data.city
        shippingForm.postCode = data.postCode

        try {
            if (shippingForm.state.displayed) {
                shippingForm.state = data.state
            }
        } catch (Throwable ignored) {
            // ignored: state dropdown may not exist for some regions
        }
        shippingCta.click()

        deliveryMethodCta.click()
        waitFor { billingTitle.value() == '' }
        useDeliveryAddress.click()
        sleep(10000)

        waitFor { billingTitle.value() == data.title }
        return this
    }

    B2cCheckoutPage fillSopCard(String type, String number, String cvv)
    {
        card.type = type
        card.number = number
        card.expMonth = EXP_MONTH
        card.expYear = EXP_YEAR
        card.cvv = cvv
        return this
    }

    B2cCheckoutPage fillFlexFormCard(String number, String cvv)
    {
        withFrame(flexMicroform.cardForm) {
            flexCardNumber = number
        }

        flexMicroform.expMonth = EXP_MONTH
        flexMicroform.expYear = EXP_YEAR

        withFrame(flexMicroform.cvvForm) {
            flexSecureCode = cvv
        }

        return this
    }

    B2cCheckoutPage fill3dSecure2()
    {
        waitFor(60) { sopRequestFrame.size() > 0 }
        withFrame(sopRequestFrame) {
            browser.at(Secure3D2PopUpPage).fill3dSecure2InFrameSA()
        }
        return this
    }

    B2cCheckoutPage placeOrder()
    {
        acceptCheckBox.click()
        placeOrderCta.click()
        return this
    }

    B2cCheckoutPage buyWithGooglePay()
    {
        acceptCheckBox.click()
        placeOrderGooglePayCta.click()
        return this
    }

}
