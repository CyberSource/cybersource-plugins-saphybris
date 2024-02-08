package isv.sap.payment.pageobject.page.secure3d

import geb.Page

class Secure3D2PopUpPage extends Page
{
    static at = { authFrame }
    static content = {
        authFrame(wait: true) { $('iframe#Cardinal-CCA-IFrame') }
        password(wait: true) { $('input', name: 'challengeDataEntry') }
        submitBtn(wait: true) { $('input[type=submit].button.primary') }
    }

    void fill3dSecure2InFrame()
    {
        withFrame(authFrame) {
            waitFor { password.displayed }
            password = '1234'
            submitBtn.click()
        }
    }
}
