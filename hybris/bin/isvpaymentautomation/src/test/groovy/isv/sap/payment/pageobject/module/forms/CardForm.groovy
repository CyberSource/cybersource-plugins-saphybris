package isv.sap.payment.pageobject.module.forms

import geb.Module

class CardForm extends Module
{
    static base = { $('form', id: 'silentOrderPostForm') }
    static content = {
        type { $(id: 'card_cardType') }
        name { $(id: 'card_nameOnCard') }
        number { $(id: 'card_accountNumber') }
        expMonth { $(id: 'ExpiryMonth') }
        expYear { $(id: 'ExpiryYear') }
        cvv { $(id: 'card_cvNumber') }

        errorMessage { $(id: 'card_cardType_errors') }
    }
}
