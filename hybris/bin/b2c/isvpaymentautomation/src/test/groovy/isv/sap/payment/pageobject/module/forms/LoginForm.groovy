package isv.sap.payment.pageobject.module.forms

import geb.Module

class LoginForm extends Module
{
    static base = { $('form#loginForm') }
    static content = {
        userName { $('#j_username') }
        password { $('#j_password') }
        login { $('button') }
    }
}
