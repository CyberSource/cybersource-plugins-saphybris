package isv.sap.payment.addon.b2b.facade.assertions;

import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.apache.commons.collections.CollectionUtils;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

public final class OrderAssertions
{
    private OrderAssertions()
    {
        // empty
    }

    public static void assertPaymentTypeSupported(final CartData cartData)
    {
        if (!isSupportedPaymentType(cartData.getPaymentType().getCode()))
        {
            throw new EntityValidationException(getLocalizedString("cart.paymentInfo.empty"));
        }
    }

    public static void assertDeliveryModeNotEmpty(final CartData cartData)
    {
        if (cartData.getDeliveryMode() == null)
        {
            throw new EntityValidationException(getLocalizedString("cart.deliveryMode.invalid"));
        }
    }

    public static void assertDeliveryAddressNotEmpty(final CartData cartData)
    {
        if (cartData.getDeliveryAddress() == null)
        {
            throw new EntityValidationException(getLocalizedString("cart.deliveryAddress.invalid"));
        }
    }

    public static void assertCartCalculated(final CartData cartData)
    {
        if (!cartData.isCalculated())
        {
            throw new EntityValidationException(getLocalizedString("cart.not.calculated"));
        }
    }

    public static boolean isSupportedPaymentType(final String code)
    {
        return CheckoutPaymentType.ACCOUNT.getCode().equals(code) || CheckoutPaymentType.CARD.getCode().equals(code);
    }

    public static void assertOrderTermsChecked(final PlaceOrderData placeOrderData)
    {
        if (!placeOrderData.getTermsCheck().equals(Boolean.TRUE))
        {
            throw new EntityValidationException(getLocalizedString("cart.term.unchecked"));
        }
    }

    public static void assertReplenishmentRecurrenceSet(final PlaceOrderData placeOrderData)
    {
        if (B2BReplenishmentRecurrenceEnum.WEEKLY.equals(placeOrderData.getReplenishmentRecurrence())
                && CollectionUtils.isEmpty(placeOrderData.getNDaysOfWeek()))
        {
            throw new EntityValidationException(getLocalizedString("cart.replenishment.no.frequency"));
        }
    }

    public static void assertReplenishmentStartDateNotNull(final PlaceOrderData placeOrderData)
    {
        if (placeOrderData.getReplenishmentStartDate() == null)
        {
            throw new EntityValidationException(getLocalizedString("cart.replenishment.no.startdate"));
        }
    }
}
