package isv.sap.payment.pageobject.module.forms

import geb.Module

class ShippingAddressForm extends Module
{
    static base = { $('form', id: 'addressForm') }
    static content = {
        country { $('select', id: 'address.country') }
        title(wait: true) { $('select', id: 'address.title') }
        firstName { $('input', id: 'address.firstName') }
        lastName { $('input', id: 'address.surname') }
        addressLine1 { $('input', id: 'address.line1') }
        addressLine2 { $('input', id: 'address.line2') }
        city { $('input', id: 'address.townCity') }
        state { $('select', id: 'address.region') }
        postCode { $('input', id: 'address.postcode') }
        phoneNumber { $('input', id: 'address.phone') }
    }
}
