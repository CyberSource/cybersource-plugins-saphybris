package isv.sap.payment.utils

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import org.joda.time.DateTime
import org.junit.Test
import spock.lang.Specification

@UnitTest
class PaymentTransactionUtilsSpec extends Specification
{
    @Test
    def 'getTransactionWithTheLatestEntry: Should return empty if there is no transactions provided'()
    {
        expect:
        PaymentTransactionUtils.getTransactionWithTheLatestEntry(transactions) == Optional.empty()

        where:
        transactions | _
        null         | _
        []           | _
    }

    @Test
    def 'getTransactionWithTheLatestEntry: Should return empty if transactions dont have entries'()
    {
        given:
        PaymentTransactionModel txn1 = new PaymentTransactionModel()
        txn1.entries = []
        PaymentTransactionModel txn2 = new PaymentTransactionModel()
        txn2.entries = []

        expect:
        PaymentTransactionUtils.getTransactionWithTheLatestEntry([txn1, txn2]) == Optional.empty()
    }

    @Test
    def 'getTransactionWithTheLatestEntry: Should find transation with the latest entry'()
    {
        given:
        PaymentTransactionModel txn1 = new PaymentTransactionModel()
        createTxnEntry(txn1, 2017, 1, 24, 13, 15)

        PaymentTransactionModel txn2 = new PaymentTransactionModel()
        createTxnEntry(txn2, 2017, 1, 24, 14, 15)
        createTxnEntry(txn2, 2017, 1, 24, 12, 15)

        PaymentTransactionModel txn3 = new PaymentTransactionModel()
        createTxnEntry(txn3, 2017, 1, 24, 14, 14)

        expect:
        PaymentTransactionUtils.getTransactionWithTheLatestEntry([txn1, txn2, txn3]).get() == txn2
    }

    @Test
    def 'isAlternativePayment: Should check if transaction is for alternative payment'()
    {
        given:
        def transaction = new PaymentTransactionModel()
        transaction.setPaymentProvider(paymentProvider)

        when:
        def result = PaymentTransactionUtils.isAlternativePayment(transaction)
        then:
        result == isAlternative

        where:
        paymentProvider       | isAlternative
        'ALTERNATIVE_PAYMENT' | true
        'PAY_PAL'             | true
        'CREDIT_CARD'         | false
        'TAX_CALCULATION'     | false
        'FRAUD'               | false
        'VISA_CHECKOUT'       | false
        'VERIFICATION'        | false
        'GIFT'                | false
        'APPLE_PAY'           | false
    }

    @SuppressWarnings('ParameterCount')
    private void createTxnEntry(PaymentTransactionModel txn, year, month, day, hour, minute)
    {
        if (txn.entries == null)
        {
            txn.entries = []
        }
        PaymentTransactionEntryModel e = new PaymentTransactionEntryModel()
        e.setTime(new DateTime(year, month, day, hour, minute).toDate())
        txn.entries << e
        e.paymentTransaction = txn
    }
}
