package isv.sap.payment.pageobject.page.alternative.wechatpay

import geb.Page

class WeChatPaymentPage extends Page
{
    static at = { pay.displayed }
    static content = {
        pay { $('button.btn-wechat-complete-payment') }
    }

    void acceptPayment() {
        def mainWindow = browser.driver.windowHandle
        sleep(5000)
        browser.driver.switchTo().window(mainWindow)
        sleep(5000)
        pay.click()
    }

    }

