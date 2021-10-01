package isv.sap.payment.addon.b2b.helper;

import de.hybris.platform.core.model.order.CartModel;

import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

public interface B2bPaymentAuthorizationHelper
{
    IsvPaymentTransactionEntryModel authorizePayment(final IsvPaymentTransactionEntryModel entry,
            final CartModel cart);

    IsvPaymentTransactionEntryModel authorizeRecurringPayment(final IsvPaymentTransactionEntryModel entry,
            final CartModel cart);
}
