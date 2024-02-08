package isv.sap.payment.pageobject.module

import geb.Module

class AsmCustomerModule extends Module
{
    static content = {
        customer(wait: true) { $('input.form-control', name: 'customerName') }
        selectItem(wait: true) { $('li.ui-menu-item') }
        startSession { $('button.ASM-btn-start-session') }
    }
}
