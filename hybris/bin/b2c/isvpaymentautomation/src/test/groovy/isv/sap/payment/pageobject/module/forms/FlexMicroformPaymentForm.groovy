package isv.sap.payment.pageobject.module.forms

import geb.Module

class FlexMicroformPaymentForm extends Module
{
    static base = { $('form', id: 'flexMicroformPaymentForm') }
    static content = {
        cardForm(wait: true) { $('div#flexCardNumber-container > iframe') }

        expMonth { $('#ExpiryMonth') }
        expYear { $('#ExpiryYear') }

        cvvForm { $('div#flexSecurityCode-container > iframe') }

        accountNumberError { $(id: 'card_accountNumber_errors') }
    }
}
