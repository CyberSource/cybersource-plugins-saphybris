package isv.sap.payment.addon.b2b.populator;

import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.converters.Populator;

import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel;

import static de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum.valueOf;
import static org.apache.commons.lang.StringUtils.EMPTY;

public class ReplenishmentPlaceOrderPopulator implements Populator<ReplenishmentInfoModel, PlaceOrderData>
{
    @Override
    public void populate(final ReplenishmentInfoModel source, final PlaceOrderData target)
    {
        target.setTermsCheck(true);
        target.setReplenishmentOrder(true);

        target.setReplenishmentStartDate(source.getStartDate());
        target.setReplenishmentRecurrence(valueOf(source.getRecurrence().toString()));
        target.setNDays(source.getDay() == null ? EMPTY : source.getDay().toString());
        target.setNthDayOfMonth(source.getDayOfMonth() == null ? EMPTY : source.getDayOfMonth().toString());
        target.setNWeeks(source.getWeek() == null ? EMPTY : source.getWeek().toString());
        target.setNDaysOfWeek(source.getDaysOfWeek());
    }
}
