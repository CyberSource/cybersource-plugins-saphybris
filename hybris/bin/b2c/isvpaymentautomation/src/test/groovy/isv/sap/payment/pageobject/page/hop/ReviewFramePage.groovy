package isv.sap.payment.pageobject.page.hop

import geb.Page

class ReviewFramePage extends Page
{
    static atCheckWaiting = true
    static at = { $('#review') }
    static content = {
        completeBtn(wait: true) { $('input.complete') }
    }

    void clickPayBtn()
    {
        waitFor { completeBtn.isDisplayed() }
        completeBtn.click()
    }
}
