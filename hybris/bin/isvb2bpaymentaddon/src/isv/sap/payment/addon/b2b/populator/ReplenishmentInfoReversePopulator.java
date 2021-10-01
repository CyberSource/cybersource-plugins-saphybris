package isv.sap.payment.addon.b2b.populator;

import java.util.List;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.cronjob.enums.DayOfWeek;

import isv.sap.payment.addon.b2b.ReplenishmentInfoData;
import isv.sap.payment.addon.b2b.enums.Recurrence;
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel;

import static java.util.stream.Collectors.toList;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class ReplenishmentInfoReversePopulator implements Populator<ReplenishmentInfoData, ReplenishmentInfoModel>
{
    private String datePattern = "MM/DD/YYYY";

    @Override
    public void populate(final ReplenishmentInfoData source, final ReplenishmentInfoModel target)
    {
        target.setDay(source.getDay());
        target.setDayOfMonth(source.getDayOfMonth());
        target.setWeek(source.getWeek());
        target.setDaysOfWeek(toEnum(source.getDaysOfWeek()));
        target.setStartDate(forPattern(datePattern).parseDateTime(source.getStartDate()).toDate());
        target.setRecurrence(Recurrence.valueOf(source.getRecurrence()));
    }

    private List<DayOfWeek> toEnum(final List<String> values)
    {
        return values.stream().map(DayOfWeek::valueOf).collect(toList());
    }

    public void setDatePattern(final String datePattern)
    {
        this.datePattern = datePattern;
    }
}
