package isv.sap.payment.pageobject.page.asm

import geb.Page

import isv.sap.payment.data.Credentials
import isv.sap.payment.pageobject.module.AsmLoginModule

class AsmLoginPage extends Page
{
    static atCheckWaiting = true
    static url = '?asm=true'
    static at = { $('#_asm') }
    static content = {
        asmLogin { module AsmLoginModule }
    }

    AsmCustomerPage loginToAsm(Credentials asmCredentials)
    {
        asmLogin.userName = asmCredentials.email
        asmLogin.password = asmCredentials.password
        asmLogin.signIn.click()

        browser.at(AsmCustomerPage)
    }
}
