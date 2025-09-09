package isv.sap.payment.pageobject.page.alternative.klarna

import geb.Page
import isv.sap.payment.data.TestData
import isv.sap.payment.data.constants.PaymentConstants.Klarna

class KlarnaWidgetPage extends Page
{
    static final TIME_PRESET = 'slow'

    static at = {true}
    static content = {

        klarnaPhoneNumber(wait: true) { $('#phonePasskey') }
        klarnaCode(wait: true)  {$('#otp_field')}
        klarnaContinueBtn(wait: true) { $( 'button#onContinue') }
        klarnaPayBtn(wait: true) { $( '#buy_button') }
 }

    void submitBillingAddressForm(TestData data)
    {

        def mainWindow = browser.driver.windowHandle
        waitFor(15) {
            browser.driver.windowHandles.size() > 1
        }
        def popupWindow = (browser.driver.windowHandles - [mainWindow]).first()
        browser.driver.switchTo().window(popupWindow)

        waitFor { klarnaPhoneNumber.click() }
        klarnaPhoneNumber = data.phoneNumber

        waitFor { klarnaContinueBtn.click() }

        waitFor { klarnaCode.click() }
        klarnaCode = Klarna.ENTRY_CODE

        waitFor { klarnaPayBtn.click() }

        sleep(7000)
        browser.driver.switchTo().window(mainWindow)
    }

}
