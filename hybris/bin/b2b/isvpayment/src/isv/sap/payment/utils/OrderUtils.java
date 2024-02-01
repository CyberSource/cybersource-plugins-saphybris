package isv.sap.payment.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

public final class OrderUtils
{
    private OrderUtils()
    {
    }

    public static BigDecimal getUnitPrice(final AbstractOrderEntryModel orderEntry)
    {
        final int scale = orderEntry.getOrder().getCurrency().getDigits();

        return BigDecimal.valueOf(orderEntry.getTotalPrice())
                .divide(BigDecimal.valueOf(orderEntry.getQuantity()), scale, RoundingMode.HALF_UP);
    }
}
