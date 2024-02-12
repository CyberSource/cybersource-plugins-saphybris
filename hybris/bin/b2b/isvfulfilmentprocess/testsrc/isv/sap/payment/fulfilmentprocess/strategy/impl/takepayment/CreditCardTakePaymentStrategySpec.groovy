package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment

import com.google.common.base.Optional
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.PaymentProcessor
import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.enums.ProcessingLevel
import isv.cjl.payment.service.executor.request.converter.processinglevel.param.ProcessingLevelParam
import isv.cjl.payment.service.processinglevel.ProcessingLevelService
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW
import static java.util.Optional.empty
import static java.util.Optional.of

@UnitTest
class CreditCardTakePaymentStrategySpec extends Specification
{
    def paymentTransactionService = Mock(PaymentTransactionService)

    def processingLevelService = Mock(ProcessingLevelService)

    def strategy = new CreditCardTakePaymentStrategy(paymentTransactionService: paymentTransactionService, processingLevelService: processingLevelService)

    @Test
    def 'should return credit card payment type'()
    {
        expect:
        strategy.paymentType == PaymentType.CREDIT_CARD
    }

    @Test
    def 'should create credit card capture request without processing level data'()
    {
        given:
        def order = new OrderModel()
        def transaction = new IsvPaymentTransactionModel(merchantId: 'mid')
        def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

        and:
        strategy.paymentTransactionService.getTransactionCardTypeNew(transaction) >> empty()
        paymentTransactionService.getLatestTransactionEntry(transaction, de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION, ACCEPT, REVIEW) >> of(transactionEntry)

        when:
        def capture = strategy.request(order, transaction)
        def params = capture.requestParams

        then:
        capture.paymentType == PaymentType.CREDIT_CARD
        capture.paymentTransactionType == PaymentTransactionType.CAPTURE

        params.merchantId == transaction.merchantId
        params.order == order
        params.transaction == transactionEntry

        and: 'processing level data is not included within request'
        params.processingLevel == null
        params.paymentProcessor == null
    }

    @Test
    def 'should create credit card capture request with processing level data'()
    {
        given:
        def order = new OrderModel()
        def transaction = new IsvPaymentTransactionModel(merchantId: 'mid')
        def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        def param = new ProcessingLevelParam(ProcessingLevel.L2, PaymentProcessor.OMNIPAY_DIRECT, CardType.VISA)

        and:
        paymentTransactionService.getTransactionCardTypeNew(transaction) >> of(CardType.VISA)
        processingLevelService.getParam(CardType.VISA) >> Optional.of(param)
        paymentTransactionService.getLatestTransactionEntry(transaction, de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION, ACCEPT, REVIEW) >> of(transactionEntry)

        when:
        def capture = strategy.request(order, transaction)
        def params = capture.requestParams

        then:
        capture.paymentType == PaymentType.CREDIT_CARD
        capture.paymentTransactionType == PaymentTransactionType.CAPTURE

        params.merchantId == transaction.merchantId
        params.order == order
        params.transaction == transactionEntry

        and: 'processing level data is included within request'
        params.processingLevel == param.level
        params.paymentProcessor == param.processor
    }
}
