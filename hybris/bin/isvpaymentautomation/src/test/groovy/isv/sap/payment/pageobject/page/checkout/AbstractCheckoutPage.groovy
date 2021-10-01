package isv.sap.payment.pageobject.page.checkout

import geb.Page

import isv.sap.payment.pageobject.module.forms.CardForm
import isv.sap.payment.pageobject.module.forms.FlexMicroformPaymentForm
import isv.sap.payment.pageobject.module.forms.ShippingAddressForm

class AbstractCheckoutPage extends Page
{
    static url = 'checkout/multi/payment-method/add'
    static atCheckWaiting = true
    static at = { $('body.page-multiStepCheckoutSummaryPage') }
    static content = {
        shippingForm { module ShippingAddressForm }
        card(wait: true) { module CardForm }

        flexMicroform { module FlexMicroformPaymentForm }
        flexCardNumber(wait: true) { $('input', name: 'number') }
        flexSecureCode { $(name: 'securityCode') }

        shippingCta { $('#addressSubmit') }
        deliveryMethodCta { $('#deliveryMethodSubmit') }
        acceptCheckBox { $('#Terms1', 0) }

        placeOrderCta(toWait: true) { $('#placeOrder', 0) }
        placeOrderGooglePayCta(toWait: true) { $('.gpay-button', 0) }

        globalError(wait: true) { $('div.global-alerts') }

        billingTitle(wait: true) { $('select', id: 'address.title') }
        useDeliveryAddress { $('label', for: 'useDeliveryAddress') }
        paymentCta(wait: true) { $('button.submit_silentOrderPostForm') }
    }
}
