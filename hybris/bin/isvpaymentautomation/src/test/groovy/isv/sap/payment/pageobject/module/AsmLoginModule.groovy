package isv.sap.payment.pageobject.module

import geb.Module

import isv.sap.payment.pageobject.page.asm.AsmCustomerPage

class AsmLoginModule extends Module
{
    static base = { $('#_asmLogin') }
    static content = {
        userName { $('input', name: 'username') }
        password { $('input', name: 'password') }
        signIn(to: AsmCustomerPage, toWait: true) { $('button.ASM-btn-login') }
    }
}
