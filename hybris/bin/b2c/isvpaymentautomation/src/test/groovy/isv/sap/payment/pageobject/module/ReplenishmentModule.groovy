package isv.sap.payment.pageobject.module

import geb.Module

class ReplenishmentModule extends Module
{
    static content = {
        calendar { $('i.glyphicon-calendar') }
        today(wait: true) { $('a.ui-state-highlight') }
        startDate(wait: true) { $('#replenishmentStartDate') }
        dailySelect { $('#replenishmentFrequencyD') }
        scheduleBtn(wait: true) { $('#placeReplenishmentOrder') }
    }
}

