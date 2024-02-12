package isv.sap.payment.addon.b2b.facade.impl;

import java.util.Optional;
import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import de.hybris.platform.util.Config;
import isv.sap.payment.addon.b2b.facade.assertions.OrderAssertions;
import isv.sap.payment.addon.b2b.helper.B2bPaymentAuthorizationHelper;
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel;
import isv.sap.payment.addon.b2b.service.B2bPaymentTransactionService;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.payment.enums.PaymentTransactionType.CREATE_SUBSCRIPTION;
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD;
import static isv.sap.payment.utils.PaymentTransactionUtils.getTransactionWithTheLatestEntry;

/**
 * An implementation of {@link PaymentCheckoutFacade} that is based on {@link DefaultB2BAcceleratorCheckoutFacade}.
 * <p>
 * Provides basic implementation of checkout-related operations on order: placement and validation.
 */
public class IsvB2BAcceleratorCheckoutFacade extends DefaultB2BAcceleratorCheckoutFacade implements
        PaymentCheckoutFacade
{
    @Resource
    private Converter<ReplenishmentInfoModel, PlaceOrderData> replenishmentPlaceOrderConverter;

    @Resource
    private B2bPaymentTransactionService b2bPaymentTransactionService;

    @Resource
    private B2bPaymentAuthorizationHelper b2bPaymentAuthorizationHelper;

    @Override
    public AbstractOrderData performPlaceOrder(final CartModel cart) throws InvalidCartException
    {
        if (cart != null)
        {
            beforePlaceOrder(cart);

            if (cart.getReplenishmentInfo() != null)
            {
                return placeReplenishmentOrder(cart);
            }

            final OrderModel order = placeOrder(cart);

            afterPlaceOrder(cart, order);

            if (order != null)
            {
                return getOrderConverter().convert(order);
            }
        }

        return null;
    }

    @Override
    protected void beforePlaceOrder(final CartModel cart)
    {
        superBeforePlaceOrder(cart);
        String transactionType = Config.getString("secure.acceptance.powertools.transaction.type", StringUtils.EMPTY);

        if (isCreditCardOrder(cart) && cart.getReplenishmentInfo() == null && transactionType.equals("create_payment_token") )
        {
                final IsvPaymentTransactionEntryModel subscriptionEntry = b2bPaymentTransactionService
                        .getLatestAcceptedTransactionEntry(CREDIT_CARD, CREATE_SUBSCRIPTION, cart);
                b2bPaymentAuthorizationHelper.authorizePayment(subscriptionEntry, cart);
            }
    }

    protected void superBeforePlaceOrder(final CartModel cart)
    {
        super.beforePlaceOrder(cart);
    }

    private boolean isCreditCardOrder(final CartModel cart)
    {
        final Optional<PaymentTransactionModel> lastTransaction = getTransactionWithTheLatestEntry(
                cart.getPaymentTransactions());

        return lastTransaction
                .map(transaction -> CREDIT_CARD.name().equals(transaction.getPaymentProvider()))
                .orElse(false);
    }

    @Override
    protected void afterPlaceOrder(final CartModel cart, final OrderModel order)
    {
        if (order != null)
        {
            if (getCartService().hasSessionCart())
            {
                getCartService().removeSessionCart();
            }
            else
            {
                getModelService().remove(cart);
            }

            getModelService().refresh(order);
        }
    }

    @Override
    public boolean validOrder(final CartModel cart)
    {
        return cart.getDeliveryAddress() != null && cart.getDeliveryMode() != null && cart.getPaymentInfo() != null;
    }

    @Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException
    {
        OrderAssertions.assertOrderTermsChecked(placeOrderData);

        if (isValidCheckoutCart(placeOrderData))
        {
            if (Boolean.TRUE.equals(placeOrderData.getReplenishmentOrder()))
            {
                OrderAssertions.assertReplenishmentStartDateNotNull(placeOrderData);

                OrderAssertions.assertReplenishmentRecurrenceSet(placeOrderData);

                final TriggerData triggerData = new TriggerData();
                populateTriggerDataFromPlaceOrderData(placeOrderData, triggerData);

                return (T) scheduleOrder(triggerData);
            }

            return (T) placeOrder();
        }

        return null;
    }

    @Override
    protected boolean isValidCheckoutCart(final PlaceOrderData placeOrderData)
    {
        final CartData cartData = getCheckoutCart();

        OrderAssertions.assertCartCalculated(cartData);

        OrderAssertions.assertDeliveryAddressNotEmpty(cartData);

        OrderAssertions.assertDeliveryModeNotEmpty(cartData);

        OrderAssertions.assertPaymentTypeSupported(cartData);

        return true;
    }

    protected AbstractOrderData placeReplenishmentOrder(final CartModel cart) throws InvalidCartException
    {
        Preconditions.checkNotNull(cart.getReplenishmentInfo());

        final PlaceOrderData placeOrderData = replenishmentPlaceOrderConverter.convert(cart.getReplenishmentInfo());

        return this.placeOrder(placeOrderData);
    }
}
