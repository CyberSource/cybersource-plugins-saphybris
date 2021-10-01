package isv.sap.payment.addon.b2b.populator

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel

import static com.google.common.collect.Lists.newArrayList
import static de.hybris.platform.cronjob.enums.DayOfWeek.MONDAY
import static de.hybris.platform.cronjob.enums.DayOfWeek.TUESDAY
import static isv.sap.payment.addon.b2b.enums.Recurrence.DAILY

@UnitTest
class ReplenishmentPlaceOrderPopulatorSpec extends Specification
{
    def source = new ReplenishmentInfoModel(startDate: new Date(), recurrence: DAILY, day: 1,
                                            dayOfMonth: 2, week: 3, daysOfWeek: newArrayList(MONDAY, TUESDAY))

    @Test
    def 'should populate place order data'()
    {
        given:
        def target = new PlaceOrderData()

        when:
        new ReplenishmentPlaceOrderPopulator().populate(source, target)

        then:
        target.replenishmentOrder == true
        target.termsCheck == true
        target.replenishmentStartDate == source.startDate
        target.replenishmentRecurrence == B2BReplenishmentRecurrenceEnum.DAILY
        target.NDays == '1'
        target.nthDayOfMonth == '2'
        target.NWeeks == '3'
        target.NDaysOfWeek == newArrayList(MONDAY, TUESDAY)
    }
}
