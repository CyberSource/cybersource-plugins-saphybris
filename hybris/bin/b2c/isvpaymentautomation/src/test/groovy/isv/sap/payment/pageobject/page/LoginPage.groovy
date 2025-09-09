package isv.sap.payment.pageobject.page

import geb.Page

import isv.sap.payment.pageobject.module.forms.LoginForm

class LoginPage extends Page
{
    static url = 'login'
    static at = { $('body.page-login') }
    static content = {
        loginForm(wait: true) { module LoginForm }
    }

    void login(String user, String loginCode)
    {
        loginForm.userName = user
        loginForm.password = loginCode
        loginForm.login.click()
    }
}
