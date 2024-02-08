package isv.sap.payment.addon.b2b.action.replenishment;

import javax.annotation.Resource;

import de.hybris.platform.b2bacceleratoraddon.actions.replenishment.AuthorizePaymentAction;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.core.model.order.CartModel;

import isv.sap.payment.addon.b2b.helper.B2bPaymentAuthorizationHelper;
import isv.sap.payment.addon.b2b.service.B2bPaymentTransactionService;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.CARD;
import static de.hybris.platform.payment.enums.PaymentTransactionType.CREATE_SUBSCRIPTION;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.NOK;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK;
import static isv.sap.payment.constants.IsvPaymentConstants.SAResponseFields.CARD_TYPE;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class IsvAuthorizePaymentAction extends AuthorizePaymentAction
{
    @Resource
    private B2bPaymentTransactionService b2bPaymentTransactionService;

    @Resource
    private B2bPaymentAuthorizationHelper b2bPaymentAuthorizationHelper;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public Transition executeAction(final ReplenishmentProcessModel process) throws Exception // NOPMD
    {
        final CartModel originalCart = process.getCartToOrderCronJob().getCart();

        if (CARD.equals(originalCart.getPaymentType()))
        {
            final CartModel clonedCart = clonedCart(process);

            final IsvPaymentTransactionEntryModel subscriptionEntry = b2bPaymentTransactionService
                    .getLatestAcceptedTransactionEntry(CREDIT_CARD, CREATE_SUBSCRIPTION, originalCart);

            final String cardType = subscriptionEntry.getProperties().get(CARD_TYPE);

            final IsvPaymentTransactionEntryModel authorization = b2bPaymentAuthorizationHelper
                    .authorizeRecurringPayment(subscriptionEntry, clonedCart);

            if (isNotBlank(cardType))
            {
                paymentTransactionService.addProperty(CARD_TYPE, cardType, authorization);
            }

            final String status = b2bPaymentAuthorizationHelper.authorizeRecurringPayment(subscriptionEntry, clonedCart)
                    .getTransactionStatus();

            return status.equals(ACCEPT) ? OK : NOK;
        }
        else
        {
            return super.executeAction(process);
        }
    }

    private CartModel clonedCart(final ReplenishmentProcessModel process)
    {
        final CartModel cart = (CartModel) processParameterHelper.getProcessParameterByName(process, "cart").getValue();
        getModelService().refresh(cart);
        return cart;
    }
}
