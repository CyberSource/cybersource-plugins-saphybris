package isv.sap.payment.addon.b2b.service

import java.time.ZoneOffset

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD
import static java.time.LocalDateTime.now

@UnitTest
class DefaultB2bPaymentTransactionServiceSpec extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionModel)

    def b2bPaymentTransactionService = new DefaultB2bPaymentTransactionService()

    @Test
    def 'get latest authorization transaction entry'()
    {
        given:
        transaction.paymentProvider >> CREDIT_CARD
        def transactionEntry1 = createTransactionEntry(Date.from(now().minusHours(1).toInstant(ZoneOffset.UTC)))
        def transactionEntry2 = createTransactionEntry(Date.from(now().toInstant(ZoneOffset.UTC)))
        transaction.entries >> [transactionEntry1, transactionEntry2]
        order.paymentTransactions >> [transaction]

        when:
        def entry = b2bPaymentTransactionService.getLatestAcceptedTransactionEntry(CREDIT_CARD, AUTHORIZATION, order)

        then:
        entry == transactionEntry2
    }

    def createTransactionEntry(final Date createdOn)
    {
        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        entry.paymentTransaction >> transaction
        entry.transactionStatus >> ACCEPT
        entry.type >> AUTHORIZATION
        entry.creationtime >> createdOn
        entry
    }
}
