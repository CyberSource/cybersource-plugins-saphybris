package isv.sap.payment.addon.b2b.facade.impl;

import java.util.Optional;
import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BAcceleratorCheckoutFacade;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;

import isv.sap.payment.addon.b2b.helper.B2bPaymentAuthorizationHelper;
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel;
import isv.sap.payment.addon.b2b.service.B2bPaymentTransactionService;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static de.hybris.platform.payment.enums.PaymentTransactionType.CREATE_SUBSCRIPTION;
import static de.hybris.platform.util.localization.Localization.getLocalizedString;
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
    @SuppressWarnings("VisibilityModifier")
    protected B2BOrderService b2bOrderService;

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
            else
            {
                final OrderModel order = placeOrder(cart);

                afterPlaceOrder(cart, order);

                if (order != null)
                {
                    return getOrderConverter().convert(order);
                }
            }
        }

        return null;
    }

    @Override
    protected void beforePlaceOrder(final CartModel cart)
    {
        superBeforePlaceOrder(cart);

        if (isCreditCardOrder(cart) && cart.getReplenishmentInfo() == null)
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
        if (!placeOrderData.getTermsCheck().equals(Boolean.TRUE))
        {
            throw new EntityValidationException(getLocalizedString("cart.term.unchecked"));
        }

        if (isValidCheckoutCart(placeOrderData))
        {
            if (placeOrderData.getReplenishmentOrder() != null && placeOrderData.getReplenishmentOrder().equals(
                    Boolean.TRUE))
            {
                if (placeOrderData.getReplenishmentStartDate() == null)
                {
                    throw new EntityValidationException(getLocalizedString("cart.replenishment.no.startdate"));
                }

                if (placeOrderData.getReplenishmentRecurrence().equals(B2BReplenishmentRecurrenceEnum.WEEKLY)
                        && CollectionUtils.isEmpty(placeOrderData.getNDaysOfWeek()))
                {
                    throw new EntityValidationException(getLocalizedString("cart.replenishment.no.frequency"));
                }

                final TriggerData triggerData = new TriggerData();
                populateTriggerDataFromPlaceOrderData(placeOrderData, triggerData);

                return (T) scheduleOrder(triggerData);
            }

            return (T) super.placeOrder();
        }

        return null;
    }

    @Override
    protected boolean isValidCheckoutCart(final PlaceOrderData placeOrderData)
    {
        final CartData cartData = getCheckoutCart();

        if (!cartData.isCalculated())
        {
            throw new EntityValidationException(getLocalizedString("cart.not.calculated"));
        }

        if (cartData.getDeliveryAddress() == null)
        {
            throw new EntityValidationException(getLocalizedString("cart.deliveryAddress.invalid"));
        }

        if (cartData.getDeliveryMode() == null)
        {
            throw new EntityValidationException(getLocalizedString("cart.deliveryMode.invalid"));
        }

        if (!isSupportedPaymentType(cartData.getPaymentType().getCode()))
        {
            throw new EntityValidationException(getLocalizedString("cart.paymentInfo.empty"));
        }

        return true;
    }

    protected ScheduledCartData placeReplenishmentOrder(final CartModel cart) throws InvalidCartException
    {
        Preconditions.checkNotNull(cart.getReplenishmentInfo());

        final PlaceOrderData placeOrderData = replenishmentPlaceOrderConverter.convert(cart.getReplenishmentInfo());

        return this.placeOrder(placeOrderData);
    }

    private boolean isSupportedPaymentType(final String code)
    {
        return code.equals(CheckoutPaymentType.ACCOUNT.getCode()) || code.equals(CheckoutPaymentType.CARD.getCode());
    }

    public void setB2bPaymentTransactionService(
            final B2bPaymentTransactionService b2bPaymentTransactionService)
    {
        this.b2bPaymentTransactionService = b2bPaymentTransactionService;
    }

    public void setB2bPaymentAuthorizationHelper(
            final B2bPaymentAuthorizationHelper b2bPaymentAuthorizationHelper)
    {
        this.b2bPaymentAuthorizationHelper = b2bPaymentAuthorizationHelper;
    }
}
