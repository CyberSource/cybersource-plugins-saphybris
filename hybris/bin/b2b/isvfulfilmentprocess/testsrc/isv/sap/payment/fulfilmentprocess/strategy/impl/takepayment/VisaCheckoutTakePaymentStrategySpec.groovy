package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment

import com.google.common.collect.ImmutableMap
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW
import static java.util.Optional.of

@UnitTest
class VisaCheckoutTakePaymentStrategySpec extends Specification
{
    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def strategy = new VisaCheckoutTakePaymentStrategy()

    def 'setup'()
    {
        strategy.paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)
    }

    @Test
    def 'should create payment service request'()
    {
        given:
        def order = new OrderModel()
        def entry = new IsvPaymentTransactionEntryModel()
        def transaction = new IsvPaymentTransactionModel(merchantId: 'merchantId')
        strategy.paymentTransactionService = paymentTransactionService
        paymentTransactionService.getLatestTransactionEntry(transaction, de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION, ACCEPT, REVIEW) >> of(entry)

        and:
        entry.setProperties(ImmutableMap.of('vcOrderId', 'vcOrderId123'))

        when:
        def request = strategy.request(order, transaction)
        def params = request.requestParams

        then:
        request.paymentType == VISA_CHECKOUT
        request.paymentTransactionType == PaymentTransactionType.CAPTURE

        params.order == order
        params.vcOrderId == 'vcOrderId123'
        params.transaction == entry
        params.merchantId == transaction.merchantId
    }

    @Test
    def 'should return visa checkout payment type'()
    {
        expect:
        strategy.paymentType == VISA_CHECKOUT
    }
}
