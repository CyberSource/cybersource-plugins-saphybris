package isv.sap.payment.pageobject.page.secure3d
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.*
import geb.Page

class Secure3D2PopUpPage extends Page
{
    static at = {
        !$('iframe').empty
    }
    static content = {
        authFrame(wait: true) { $('iframe#step-up-iframe-id') }
        sopauthFrame(wait: true) { $('iframe#Cardinal-CCA-IFrame') }
        codeEntry(wait: true) { $('input', name: 'challengeDataEntry') }
        submitBtn(wait: true) { $('input[type=submit].button.primary') }

    }

    void fill3dSecure2InFrame()
    {
        withFrame(authFrame) {
            withFrame(0) {
                waitFor { codeEntry.displayed }
                codeEntry = PIN_4_DIGITS
                submitBtn.click()
            }
        }
    }

    void fill3dSecure2InFrameSA() {

        withFrame(sopauthFrame) {
            waitFor { codeEntry.displayed }
            codeEntry = PIN_4_DIGITS
            submitBtn.click()
        }
        }

    }