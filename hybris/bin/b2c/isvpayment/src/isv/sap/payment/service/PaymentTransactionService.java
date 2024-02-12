package isv.sap.payment.service;

import java.util.Optional;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import isv.cjl.payment.enums.CardType;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

/**
 * Service that provides methods to work with payment transaction and transaction entries.
 */
public interface PaymentTransactionService
{
    /**
     * Gets order payment transaction that matches given payment type.
     *
     * @param paymentType the payment type to match against payment transaction
     * @param order       the order that contains payment transactions
     * @return payment transaction that matches payment type, if exists
     */
    Optional<PaymentTransactionModel> getTransaction(final PaymentType paymentType, final AbstractOrderModel order);

    /**
     * Gets order latest payment transaction that matches given payment type.
     *
     * @param paymentType the payment type to match against payment transaction
     * @param order       the order that contains payment transactions
     * @return payment transaction that matches payment type, if exists
     */
    Optional<PaymentTransactionModel> getLatestTransaction(final PaymentType paymentType,
            final AbstractOrderModel order);

    /**
     * Gets payment transaction entry that matches given type and statuses.
     *
     * @param paymentTransaction the payment transaction that contains transaction entries
     * @param type               the payment transaction type (e.g. AUTHORIZATION, CAPTURE, etc.)
     * @param statuses           the payment transaction statuses (e.g. ACCEPT, REVIEW, etc.)
     * @return payment transaction entry that matches given criteria, if exists
     */
    Optional<PaymentTransactionEntryModel> getLatestTransactionEntry(final PaymentTransactionModel paymentTransaction,
            final PaymentTransactionType type, final String... statuses);

    /**
     * Gets payment transaction entry that matches given type and in ACCEPTED status.
     *
     * @param paymentTransaction the payment transaction that contains transaction entries
     * @param type               the payment transaction type (e.g. AUTHORIZATION, CAPTURE, etc.)
     * @return payment transaction entry that matches given criteria, if exists
     */
    Optional<PaymentTransactionEntryModel> getLatestAcceptedTransactionEntry(
            final PaymentTransactionModel paymentTransaction, final PaymentTransactionType type);

    /**
     * Gets card type for given transaction if present. The first accepted transaction entry is used to
     * look for card type property.
     *
     * @param transaction transaction model
     * @return an optional card type
     */
    Optional<CardType> getTransactionCardTypeNew(final PaymentTransactionModel transaction);

    /**
     * Adds provided property to transaction entry's properties.
     *
     * @param name property name
     * @param value property value
     * @param entry transaction entry to add property for
     */
    void addProperty(final String name, String value, final IsvPaymentTransactionEntryModel entry);

    /**
     * Creates an authorization transaction entry from a bundled enrollment transaction
     * @param enrollmentTransaction enrollment transaction
     * @return authorization transaction
     */
    Optional<PaymentTransactionEntryModel> createAuthorizationTxEntryFromEnrollment(
            final IsvPaymentTransactionEntryModel enrollmentTransaction);
}
