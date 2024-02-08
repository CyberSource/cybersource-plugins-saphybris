package isv.sap.payment.pageobject.page.asm

import geb.Page

import isv.sap.payment.pageobject.module.AsmCustomerModule

class AsmCustomerPage extends Page
{
    static at = { $('#_asmCustomer') }
    static content = {
        asmCustomer { module AsmCustomerModule }
        loggedIn(wait: true) { $('li.logged_in') }
    }

    void selectUser(String email)
    {
        asmCustomer.customer = email
        asmCustomer.selectItem.click()
        asmCustomer.startSession.click()

        waitFor { loggedIn }
    }
}
