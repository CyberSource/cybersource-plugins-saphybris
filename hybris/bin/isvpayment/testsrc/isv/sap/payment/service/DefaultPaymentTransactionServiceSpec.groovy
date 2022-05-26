package isv.sap.payment.service

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.model.ModelService
import org.joda.time.LocalDateTime
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.CardType
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW

@UnitTest
class DefaultPaymentTransactionServiceSpec extends Specification
{
    def creditCardTransaction = Mock([useObjenesis: false], PaymentTransactionModel)

    def authorizationTransactionEntry1 = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def authorizationTransactionEntry2 = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def service = new DefaultPaymentTransactionService()

    def setup()
    {
        creditCardTransaction.paymentProvider >> 'CREDIT_CARD'

        authorizationTransactionEntry1.type >> PaymentTransactionType.AUTHORIZATION
        authorizationTransactionEntry1.transactionStatus >> IsvPaymentConstants.TransactionStatus.ACCEPT

        authorizationTransactionEntry2.type >> PaymentTransactionType.AUTHORIZATION
        authorizationTransactionEntry2.transactionStatus >> IsvPaymentConstants.TransactionStatus.ACCEPT

        service.modelService = Mock([useObjenesis: false], ModelService)
    }

    @Test
    def 'should get order payment transaction by payment type'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]

        when:
        def transaction = service.getTransaction(PaymentType.CREDIT_CARD, order)

        then:
        transaction.isPresent()
        transaction.get() == creditCardTransaction
    }

    @Test
    def 'should return empty if payment transaction not found for specified payment type'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]

        when:
        def transaction = service.getTransaction(PaymentType.PAY_PAL, order)

        then:
        !transaction.isPresent()
    }

    @Test
    def 'should get order latest payment transaction entry by payment type and status'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]
        creditCardTransaction.entries >> [authorizationTransactionEntry1, authorizationTransactionEntry2]

        when:
        def entry = service.getLatestTransactionEntry(creditCardTransaction, PaymentTransactionType.AUTHORIZATION,
                                                      IsvPaymentConstants.TransactionStatus.ACCEPT)

        then:
        entry.isPresent()
        entry.get() == authorizationTransactionEntry2
    }

    @Test
    def 'should return empty if payment transaction entry with same type not found'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]
        creditCardTransaction.entries >> [authorizationTransactionEntry1, authorizationTransactionEntry2]

        when:
        def entry = service.getLatestTransactionEntry(creditCardTransaction, PaymentTransactionType.CANCEL,
                                                      IsvPaymentConstants.TransactionStatus.ACCEPT)

        then:
        !entry.isPresent()
    }

    @Test
    def 'should return empty if payment transaction entry with same status not found'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]
        creditCardTransaction.entries >> [authorizationTransactionEntry1, authorizationTransactionEntry2]

        when:
        def entry = service.getLatestTransactionEntry(creditCardTransaction, PaymentTransactionType.AUTHORIZATION,
                                                      IsvPaymentConstants.TransactionStatus.REVIEW)

        then:
        !entry.isPresent()
    }

    @Test
    def 'should return last authorization payment transaction entry'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]
        creditCardTransaction.entries >> [authorizationTransactionEntry1, authorizationTransactionEntry2]

        when:
        def entry = service.getLatestAcceptedTransactionEntry(creditCardTransaction, PaymentTransactionType.AUTHORIZATION)

        then:
        entry.isPresent()
        entry.get() == authorizationTransactionEntry2
    }

    @Test
    def 'should return empty if authorization transaction entry not found'()
    {
        given:
        order.paymentTransactions >> [creditCardTransaction]
        creditCardTransaction.entries >> []

        when:
        def entry = service.getLatestAcceptedTransactionEntry(creditCardTransaction, PaymentTransactionType.AUTHORIZATION)

        then:
        !entry.isPresent()
    }

    @Test
    def 'should return lasted transaction'()
    {
        given:
        creditCardTransaction.entries >> [authorizationTransactionEntry1]

        def latestCreditCardTransaction = Mock([useObjenesis: false], PaymentTransactionModel)
        latestCreditCardTransaction.paymentProvider >> 'CREDIT_CARD'
        latestCreditCardTransaction.entries >> [authorizationTransactionEntry2]

        authorizationTransactionEntry1.time >> LocalDateTime.now().minusMinutes(10).toDate()
        authorizationTransactionEntry1.paymentTransaction >> creditCardTransaction
        authorizationTransactionEntry2.time >> LocalDateTime.now().toDate()
        authorizationTransactionEntry2.paymentTransaction >> latestCreditCardTransaction

        order.paymentTransactions >> [creditCardTransaction, latestCreditCardTransaction]

        when:
        def transaction = service.getLatestTransaction(PaymentType.CREDIT_CARD, order)

        then:
        transaction.get() == latestCreditCardTransaction
    }

    @Test
    def 'should return card type from transaction new'()
    {
        given:
        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        creditCardTransaction.entries >> [entry]

        entry.transactionStatus >> ACCEPT
        entry.properties >> ['req_card_type': isv.cjl.payment.enums.CardType.VISA.name]

        when:
        def type = service.getTransactionCardTypeNew(creditCardTransaction).get()

        then:
        type == CardType.VISA
    }

    @Test
    def 'should return empty if card type not available'()
    {
        given:
        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        creditCardTransaction.entries >> [entry]

        entry.transactionStatus >> REVIEW
        entry.properties >> ['req_card_type': '']

        when:
        def type = service.getTransactionCardTypeNew(creditCardTransaction)

        then:
        type == Optional.empty()
    }

    @Test
    def 'should add property to transaction entry'()
    {
        given:
        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        entry.properties >> [name: 'value']

        when:
        service.addProperty('newName', 'newValue', entry)

        then:
        1 * entry.setProperties([name: 'value', newName: 'newValue'])
        1 * service.modelService.save(entry)
    }

    @Test
    def 'should create an authorized transaction entry from an enrollment transaction'()
    {
        given:
        def enrollmentTxEntry = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTxEntry.properties >> ['ccAuthReplyReasonCode': '100']
        def authTxEntry = Mock(IsvPaymentTransactionEntryModel)
        and:
        service.modelService.clone(enrollmentTxEntry) >> authTxEntry

        when:
        def result = service.createAuthorizationTxEntryFromEnrollment(enrollmentTxEntry)

        then:
        1 * authTxEntry.setType(PaymentTransactionType.AUTHORIZATION)
        1 * service.modelService.save(authTxEntry)
        result.get() == authTxEntry
    }

    @Test
    def 'should not create an authorized transaction entry from a transaction that was not bundled with Auth call'()
    {
        given:
        def enrollmentTxEntry = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTxEntry.properties >> [:]

        when:
        def result = service.createAuthorizationTxEntryFromEnrollment(enrollmentTxEntry)

        then:
        0 * service.modelService.save(_)
        0 * service.modelService.clone(_)
        result == Optional.empty()
    }
}
