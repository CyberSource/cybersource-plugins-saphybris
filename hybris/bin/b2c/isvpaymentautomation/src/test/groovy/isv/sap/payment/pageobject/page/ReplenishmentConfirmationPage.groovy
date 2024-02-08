package isv.sap.payment.pageobject.page

import geb.Page

class ReplenishmentConfirmationPage extends Page
{
    static atCheckWaiting = true
    static at = { $('body.page-replenishmentConfirmationPage') }
    static content = {
        confirmationNumber(wait: true) { $('span.item-value').first() }
    }

    String extractCronJobNumber()
    {
        confirmationNumber.text()
    }
}
