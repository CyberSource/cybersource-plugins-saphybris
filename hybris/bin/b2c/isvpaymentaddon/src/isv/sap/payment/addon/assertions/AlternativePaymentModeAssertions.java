package isv.sap.payment.addon.assertions;

import de.hybris.platform.core.model.order.payment.PaymentModeModel;

import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentModeModel;

import static org.springframework.util.Assert.notNull;

public final class AlternativePaymentModeAssertions
{
    private AlternativePaymentModeAssertions()
    {
        // EMPTY
    }

    public static void assertValidPaymentType(final IsvPaymentModeModel isvPaymentMode)
    {
        if (!PaymentType.ALTERNATIVE_PAYMENT.equals(isvPaymentMode.getPaymentType()))
        {
            throw new IllegalStateException("Expected payment type is: ALTERNATIVE_PAYMENT but got "
                    + isvPaymentMode.getPaymentType());
        }
    }

    public static void assertValidPaymentModeClazz(final PaymentModeModel paymentMode)
    {
        if (!(paymentMode instanceof IsvPaymentModeModel))
        {
            throw new IllegalStateException("Expected payment mode type is IsvPaymentModeModel");
        }
    }

    public static void assertAlternativePaymentSubTypeIsSet(final AlternativePaymentMethod alternativePaymentType)
    {
        notNull(alternativePaymentType, "IsvPaymentModeModel.paymentSubType should be specified");
    }
}
