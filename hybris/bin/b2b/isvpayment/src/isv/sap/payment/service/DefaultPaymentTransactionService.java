package isv.sap.payment.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.google.common.collect.Lists;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;

import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.enums.CardType;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.sap.payment.constants.IsvPaymentConstants.SAResponseFields.CARD_TYPE;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW;
import static isv.sap.payment.utils.PaymentTransactionUtils.getTransactionWithTheLatestEntry;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;

/**
 * Encapsulates the default implementation of {@link PaymentTransactionService} interface.
 * <p>
 * Implements transaction retrieval logic - for latest and accepted transactions.
 */
public class DefaultPaymentTransactionService implements PaymentTransactionService
{
    @Resource
    private ModelService modelService;

    @Override
    public Optional<PaymentTransactionModel> getTransaction(final PaymentType paymentType,
            final AbstractOrderModel order)
    {
        return order.getPaymentTransactions().stream()
                .filter(transaction -> paymentType.name().equals(transaction.getPaymentProvider()))
                .findFirst();
    }

    @Override
    public Optional<PaymentTransactionModel> getLatestTransaction(final PaymentType paymentType,
            final AbstractOrderModel order)
    {
        return getTransactionWithTheLatestEntry(order.getPaymentTransactions().stream()
                .filter(transaction -> paymentType.name().equals(transaction.getPaymentProvider()))
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<PaymentTransactionEntryModel> getLatestAcceptedTransactionEntry(
            final PaymentTransactionModel paymentTransaction,
            final PaymentTransactionType type)
    {
        return getLatestTransactionEntry(paymentTransaction, type,
                PaymentConstants.TransactionStatus.ACCEPT);
    }

    @Override
    public Optional<PaymentTransactionEntryModel> getLatestTransactionEntry(
            final PaymentTransactionModel paymentTransaction,
            final PaymentTransactionType type, final String... statuses)
    {
        final List<PaymentTransactionEntryModel> reversedTransactionEntries = Lists
                .newArrayList(paymentTransaction.getEntries());

        Collections.reverse(reversedTransactionEntries);

        return reversedTransactionEntries.stream()
                .filter(entry -> type.equals(entry.getType()) && Arrays.stream(statuses)
                        .anyMatch(status -> status.equals(entry.getTransactionStatus())))
                .findFirst();
    }

    @Override
    public Optional<CardType> getTransactionCardTypeNew(final PaymentTransactionModel transaction)
    {
        return transaction.getEntries()
                .stream()
                .filter(entryModel -> entryModel.getTransactionStatus().equals(ACCEPT)
                        || entryModel.getTransactionStatus().equals(REVIEW))
                .findFirst()
                .map(IsvPaymentTransactionEntryModel.class::cast)
                .map(IsvPaymentTransactionEntryModel::getProperties)
                .map(props -> props.get(CARD_TYPE))
                .filter(StringUtils::isNotBlank)
                .flatMap(type -> stream(CardType.values()).filter(value -> value.getName().equals(type)).findFirst());
    }

    @Override
    public void addProperty(final String name, final String value, final IsvPaymentTransactionEntryModel entry)
    {
        final Map<String, String> props = new HashMap<>(entry.getProperties());

        props.put(name, value);
        entry.setProperties(props);

        modelService.save(entry);
    }

    @Override
    public Optional<PaymentTransactionEntryModel> createAuthorizationTxEntryFromEnrollment(
            final IsvPaymentTransactionEntryModel enrollmentTransaction)
    {
        final boolean isAuthReply = enrollmentTransaction.getProperties().get("ccAuthReplyReasonCode") != null;
        if (isAuthReply)
        {
            final IsvPaymentTransactionEntryModel authorizationTransactionEntry = modelService
                    .clone(enrollmentTransaction);
            authorizationTransactionEntry.setType(PaymentTransactionType.AUTHORIZATION);
            modelService.save(authorizationTransactionEntry);
            return Optional.of(authorizationTransactionEntry);
        }
        return empty();
    }
}
