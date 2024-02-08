package isv.sap.payment.addon.b2b.populator

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel

import static de.hybris.platform.cronjob.enums.DayOfWeek.MONDAY
import static de.hybris.platform.cronjob.enums.DayOfWeek.TUESDAY
import static isv.sap.payment.addon.b2b.enums.Recurrence.DAILY

@UnitTest
class ReplenishmentPlaceOrderPopulatorSpec extends Specification
{
    def populator = new ReplenishmentPlaceOrderPopulator()

    @Test
    def 'should populate place order data'()
    {
        given:
        def source = Mock(ReplenishmentInfoModel)
        source.startDate >> new Date()
        source.recurrence >> DAILY
        source.day >> 1
        source.dayOfMonth >> 2
        source.week >> 3
        source.daysOfWeek >> [MONDAY, TUESDAY]
        def target = new PlaceOrderData()

        when:
        populator.populate(source, target)

        then:
        target.replenishmentOrder == true
        target.termsCheck == true
        target.replenishmentStartDate == source.startDate
        target.replenishmentRecurrence == B2BReplenishmentRecurrenceEnum.DAILY
        target.NDays == '1'
        target.nthDayOfMonth == '2'
        target.NWeeks == '3'
        target.NDaysOfWeek == [MONDAY, TUESDAY]
    }

    @Test
    def 'should populate place order data with recurrence default values'()
    {
        given:
        def source = Mock(ReplenishmentInfoModel)
        source.startDate >> new Date()
        source.recurrence >> DAILY
        source.daysOfWeek >> [MONDAY, TUESDAY]
        def target = new PlaceOrderData()

        when:
        populator.populate(source, target)

        then:
        target.replenishmentOrder == true
        target.termsCheck == true
        target.replenishmentStartDate == source.startDate
        target.replenishmentRecurrence == B2BReplenishmentRecurrenceEnum.DAILY
        target.NDays == ''
        target.nthDayOfMonth == ''
        target.NWeeks == ''
        target.NDaysOfWeek == [MONDAY, TUESDAY]
    }
}
