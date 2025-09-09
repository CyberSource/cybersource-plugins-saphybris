package isv.sap.payment.pageobject.page.googlepay

import geb.Page
import org.openqa.selenium.Keys
import isv.sap.payment.data.Credentials

class GooglePayPage extends Page {

    static atCheckWaiting = true
    static at = { title.contains('Google') }


    static content = {
        email(wait: true) { $('input[type="email"]') }
        password(wait: true) { $('input[type="password"]:not([name="hiddenPassword"])') }
        cardForm(wait: true) { $('#sM432dIframe') }
        payButton(wait: true) { $('button span', text: 'Pay').closest('button') }
    }

    void switchToGooglePayPopup() {
        def mainWindow = browser.driver.windowHandle
        waitFor(15) {
            browser.driver.windowHandles.size() > 1
        }
        def allWindows = browser.driver.windowHandles
        def popupWindow = (allWindows - [mainWindow])[0]
        browser.driver.switchTo().window(popupWindow)
    }

    GooglePayPage loginToGoogle(Credentials googleCredentials) {

        waitFor { email.displayed }
        email.value(googleCredentials.email)
        email << Keys.ENTER

        password.value(googleCredentials.usercode)
        password << Keys.ENTER
        browser.at(GooglePayPage)
    }


    void acceptPayment() {
        waitFor(15) {
            driver.switchTo().frame(cardForm.firstElement())
        }
        waitFor { payButton.displayed }
        payButton.click()
sleep(5000)
        def mainWindow = browser.driver.windowHandles.first()
        driver.switchTo().defaultContent()
        browser.driver.switchTo().window(mainWindow)
    }
}
