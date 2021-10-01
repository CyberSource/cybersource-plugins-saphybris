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

    void login(String user, String password)
    {
        loginForm.userName = user
        loginForm.password = password
        loginForm.login.click()
    }
}
