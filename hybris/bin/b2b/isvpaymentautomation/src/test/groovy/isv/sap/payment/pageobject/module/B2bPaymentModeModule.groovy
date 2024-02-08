package isv.sap.payment.pageobject.module

import geb.Module

class B2bPaymentModeModule extends Module
{
    static base = { $('div.radiobuttons_paymentselection') }
    static content = {
        card { $('#PaymentTypeSelection_CARD') }
        payPal { $('#PaymentTypeSelection_PAYPAL') }
        account { $('#PaymentTypeSelection_ACCOUNT') }
    }
}
