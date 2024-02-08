package isv.sap.payment.pageobject.page.alternative.mollie

import geb.Module

class StatusModule extends Module
{
    static content = {
        success { $('input', value: 'paid') }
        cancelled { $('input', value: 'canceled') }
        failure { $('input', value: 'failed') }
        open { $('input', value: 'open') }
    }
}
