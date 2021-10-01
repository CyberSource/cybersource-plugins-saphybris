package isv.sap.payment.addon.b2b.populator

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.ReplenishmentInfoData
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel

import static com.google.common.collect.Lists.newArrayList
import static de.hybris.platform.cronjob.enums.DayOfWeek.MONDAY
import static de.hybris.platform.cronjob.enums.DayOfWeek.TUESDAY
import static org.joda.time.format.DateTimeFormat.forPattern

@UnitTest
class ReplenishmentInfoReversePopulatorSpec extends Specification
{
    def source = new ReplenishmentInfoData(day: 1, week: 2, dayOfMonth: 3, startDate: '10/20/2020',
                                           recurrence: 'DAILY', daysOfWeek: newArrayList('MONDAY', 'TUESDAY'))

    @Test
    def 'should populate replenishment model'()
    {
        given:
        def target = new ReplenishmentInfoModel()

        when:
        new ReplenishmentInfoReversePopulator(datePattern: 'MM/DD/YYYY').populate(source, target)

        then:
        target.day == source.day
        target.week == source.week
        target.dayOfMonth == source.dayOfMonth
        target.startDate == forPattern('MM/DD/YYYY').parseDateTime(source.startDate).toDate()
        target.recurrence.toString() == source.recurrence
        target.daysOfWeek.size() == source.daysOfWeek.size()
        target.daysOfWeek == newArrayList(MONDAY, TUESDAY)
    }
}
