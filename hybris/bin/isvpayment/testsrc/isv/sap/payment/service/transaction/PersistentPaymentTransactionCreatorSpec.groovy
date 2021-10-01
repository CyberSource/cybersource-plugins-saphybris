package isv.sap.payment.service.transaction

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.strategy.TransactionCodeGenerator
import de.hybris.platform.servicelayer.internal.dao.GenericDao
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.time.TimeService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel

import static isv.sap.payment.enums.AlternativePaymentMethod.APY
import static isv.sap.payment.enums.AlternativePaymentMethod.IDL
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT

@UnitTest
class PersistentPaymentTransactionCreatorSpec extends Specification
{
    def modelService = Mock([useObjenesis: false], ModelService)

    def transactionCodeGenerator = Mock([useObjenesis: false], TransactionCodeGenerator)

    def timeService = Mock([useObjenesis: false], TimeService)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def currentTime = Mock([useObjenesis: false], Date)

    def coreRequest = PaymentServiceRequest.create()

    def paymentServiceResult = new PaymentServiceResult()

    def abstractOrderGenericDao = Mock([useObjenesis: false], GenericDao)

    @SuppressWarnings('BracesForClass')
    def creator = new PersistentPaymentTransactionCreator() {
        @Override
        protected IsvPaymentTransactionEntryModel superCreateTransactionEntry(
                final PaymentServiceRequest request, final PaymentServiceResult isvResponse)
        {
            transactionEntry
        }
    }

    def setup()
    {
        creator.superCreateTransactionEntry(coreRequest, paymentServiceResult) >> transactionEntry
        creator.modelService = modelService
        creator.transactionCodeGenerator = transactionCodeGenerator

        coreRequest.addParam('merchantId', 'merchant_1')
        coreRequest.addParam('order', order)
        coreRequest.paymentTransactionType = PaymentTransactionType.AUTHORIZATION

        paymentServiceResult.addData('requestID', '123')
        paymentServiceResult.addData('requestToken', '321')
        paymentServiceResult.addData('decision', 'ACCEPT')
        paymentServiceResult.addData('reasonCode', 100)
        paymentServiceResult.addData('purchaseTotalsCurrency', 'EUR')
        paymentServiceResult.addData('amount', '10.12')
        paymentServiceResult.addData('paySubscriptionCreateReplySubscriptionID', '4857756716436115104009')

        currency.isocode >> 'EUR'

        order.code >> '890'

        transactionCodeGenerator.generateCode('890') >> '890-1'
        timeService.currentTime >> currentTime

        transaction.code >> '890-1'
        transaction.entries >> []

        modelService.create(IsvPaymentTransactionModel) >> transaction

        abstractOrderGenericDao.find(Collections.singletonMap('code', '890')) >> [order]
    }

    @Test
    def 'creator should create and save payment transaction and add new transaction entry from call to core library'()
    {
        given:
        coreRequest.paymentType = PaymentType.ALTERNATIVE_PAYMENT
        coreRequest.addParam('apPaymentType', IDL)
        order.paymentTransactions >> []
        transaction.paymentProvider >> ALTERNATIVE_PAYMENT.code

        when:
        def result = creator.createTransactionEntry(coreRequest, paymentServiceResult)

        then:
        1 * modelService.create(IsvPaymentTransactionModel) >> transaction
        1 * transaction.setMerchantId('merchant_1')
        1 * transaction.setCode('890-1')
        1 * transaction.setRequestId('123')
        1 * transaction.setRequestToken('321')
        1 * transaction.setPaymentProvider('ALTERNATIVE_PAYMENT')
        1 * transaction.setOrder(order)
        1 * transaction.setAlternativePaymentMethod(IDL)
        1 * modelService.saveAll(transaction, order)

        1 * modelService.attach(transactionEntry)
        1 * modelService.saveAll(transactionEntry, transaction)

        result == transactionEntry
    }

    @Test
    def 'creator should find existing payment transaction and add new transaction entry from call to core library '()
    {
        given:
        coreRequest.paymentType = PaymentType.ALTERNATIVE_PAYMENT
        coreRequest.addParam('apPaymentType', IDL)
        transaction.paymentProvider >> ALTERNATIVE_PAYMENT.code
        order.paymentTransactions >> [transaction]

        when:
        def result = creator.createTransactionEntry(coreRequest, paymentServiceResult)

        then:
        0 * modelService.create(IsvPaymentTransactionModel) >> transaction
        0 * modelService.saveAll(transaction, order)

        1 * transactionEntry.setPaymentTransaction(transaction)

        1 * modelService.attach(transactionEntry)
        1 * modelService.saveAll(transactionEntry, transaction)

        result == transactionEntry
    }

    @Test
    def 'creator should work on edge cases'()
    {
        given:
        coreRequest.paymentType = null
        coreRequest.addParam('apPaymentType', null)
        order.paymentTransactions >> [transaction]
        transaction.paymentProvider >> ALTERNATIVE_PAYMENT.code

        when:
        def result = creator.createTransactionEntry(coreRequest, paymentServiceResult)

        then:
        1 * modelService.create(IsvPaymentTransactionModel) >> transaction
        1 * transaction.setMerchantId('merchant_1')
        1 * transaction.setCode('890-1')
        1 * transaction.setRequestId('123')
        1 * transaction.setRequestToken('321')
        1 * transaction.setPaymentProvider(null)
        1 * transaction.setOrder(order)
        1 * transaction.setAlternativePaymentMethod(null)
        1 * modelService.saveAll(transaction, order)

        1 * modelService.attach(transactionEntry)
        1 * modelService.saveAll(transactionEntry, transaction)

        result == transactionEntry
    }

    @Test
    def 'creator should reuse existing transaction'()
    {
        given:
        coreRequest.paymentType = PaymentType.ALTERNATIVE_PAYMENT
        coreRequest.addParam('apPaymentType', APY)
        transaction.paymentProvider >> ALTERNATIVE_PAYMENT.code
        transaction.alternativePaymentMethod >> APY
        order.paymentTransactions >> [transaction]

        when:
        def result = creator.createTransactionEntry(coreRequest, paymentServiceResult)

        then:
        0 * modelService.create(IsvPaymentTransactionModel) >> transaction
        0 * modelService.saveAll(transaction, order)

        1 * transactionEntry.setPaymentTransaction(transaction)
        1 * modelService.attach(transactionEntry)
        1 * modelService.saveAll(transactionEntry, transaction)

        result == transactionEntry
    }
}
