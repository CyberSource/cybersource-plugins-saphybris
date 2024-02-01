package isv.sap.payment.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.collections.CollectionUtils;

import isv.sap.payment.enums.PaymentType;

import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static isv.sap.payment.enums.PaymentType.PAY_PAL;

public final class PaymentTransactionUtils
{
    private PaymentTransactionUtils()
    {
        // EMPTY
    }

    public static Optional<PaymentTransactionModel> getTransactionWithTheLatestEntry(
            final List<PaymentTransactionModel> allTransactions)
    {
        if (CollectionUtils.isEmpty(allTransactions))
        {
            return Optional.empty();
        }

        return allTransactions.stream().flatMap(txn -> txn.getEntries().stream()).
                max(Comparator.comparing(PaymentTransactionEntryModel::getTime)).
                map(PaymentTransactionEntryModel::getPaymentTransaction);
    }

    public static boolean isAlternativePayment(final PaymentTransactionModel transaction)
    {
        final PaymentType paymentType = PaymentType.valueOf(transaction.getPaymentProvider());

        return ALTERNATIVE_PAYMENT.equals(paymentType) || PAY_PAL.equals(paymentType);
    }
}
