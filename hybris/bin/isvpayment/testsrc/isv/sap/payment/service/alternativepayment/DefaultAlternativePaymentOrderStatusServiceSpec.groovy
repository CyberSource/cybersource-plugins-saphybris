package isv.sap.payment.service.alternativepayment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.alternativepayment.AlternativePaymentCheckStatusService
import isv.sap.payment.configuration.service.PaymentConfigurationService
import isv.sap.payment.enums.IsvAlternativePaymentStatus
import isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
import static de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE
import static de.hybris.platform.payment.enums.PaymentTransactionType.CHECK_STATUS
import static de.hybris.platform.payment.enums.PaymentTransactionType.INITIATE
import static de.hybris.platform.payment.enums.PaymentTransactionType.SALE
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ERROR
import static isv.sap.payment.enums.AlternativePaymentMethod.APY
import static isv.sap.payment.enums.IsvConfigurationType.ALTERNATIVE_PAYMENT_CONFIG
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT
import static isv.sap.payment.enums.PaymentType.PAY_PAL
import static java.util.Optional.empty
import static java.util.Optional.of

@UnitTest
class DefaultAlternativePaymentOrderStatusServiceSpec extends Specification
{
    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def paymentConfigurationService = Mock([useObjenesis: false], PaymentConfigurationService)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def configuration = Mock([useObjenesis: false], IsvCheckAlternativePaymentStatusConfigurationModel)

    def modelService = Mock([useObjenesis: false], ModelService)

    def alternativePaymentCheckStatusService = Mock([useObjenesis: false], AlternativePaymentCheckStatusService)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def service = Spy(DefaultAlternativePaymentOrderStatusService)

    void setup()
    {
        order.guid >> '000111'
        transaction.order >> order

        service.paymentTransactionService = paymentTransactionService
        service.paymentConfigurationService = paymentConfigurationService
        service.modelService = modelService
        service.alternativePaymentCheckStatusService = alternativePaymentCheckStatusService
    }

    @Test
    def 'get payment status for order payment'()
    {
        def saleEntry = newTransactionEntry(SALE)
        saleEntry.creationtime >> new Date()

        given:
        transaction.entries >> [saleEntry, newTransactionEntry(CHECK_STATUS), newTransactionEntry(CHECK_STATUS)]
        transaction.alternativePaymentMethod >> APY

        when:
        service.shouldRunCheckStatus(transaction)

        then:
        1 * alternativePaymentCheckStatusService.shouldRun(saleEntry.creationtime, 2,
                                                           isv.cjl.payment.enums.AlternativePaymentMethod.APY,
                                                           isv.cjl.payment.enums.PaymentTransactionType.SALE) >> null
    }

    @Test
    def 'get an existing alternative payment transaction'()
    {
        given:
        paymentTransactionService.getLatestTransaction(ALTERNATIVE_PAYMENT, order) >> of(transaction)

        when:
        def result = service.getAlternativePaymentTransaction(order)

        then:
        result == transaction
    }

    @Test
    def 'get an existing alternative payment transaction with PayPal as fallback'()
    {
        given:
        paymentTransactionService.getLatestTransaction(ALTERNATIVE_PAYMENT, order) >> empty()
        paymentTransactionService.getLatestTransaction(PAY_PAL, order) >> of(transaction)

        when:
        def result = service.getAlternativePaymentTransaction(order)

        then:
        result == transaction
    }

    @Test
    def 'should throw an exception when no alternative payment transaction found'()
    {
        given:
        paymentTransactionService.getLatestTransaction(_, order) >> empty()

        when:
        service.getAlternativePaymentTransaction(order)

        then:
        thrown IllegalStateException
    }

    @Test
    def 'get an existing alternative payment transaction entry'()
    {
        given:
        paymentTransactionService.getTransaction(ALTERNATIVE_PAYMENT, order) >> of(this.transaction)
        transactionEntry = newTransactionEntry(paymentTransactionType)
        transaction.entries >> [transactionEntry]

        when:
        def result = service.getAlternativePaymentTransactionEntry(transaction)

        then:
        result == transactionEntry

        where:
        paymentTransactionType | _
        SALE                   | _
        INITIATE               | _
        AUTHORIZATION          | _
        CAPTURE                | _
    }

    @Test
    def 'should throw an exception when no accepted alternative payment transaction entry found'()
    {
        given:
        paymentTransactionService.getTransaction(ALTERNATIVE_PAYMENT, order) >> of(this.transaction)

        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        entry.type >> _
        entry.transactionStatus >> ERROR
        transaction.entries >> [entry]

        when:
        service.getAlternativePaymentTransactionEntry(transaction)

        then:
        thrown IllegalStateException
    }

    @Test
    def 'should update order with payment status'()
    {
        given:
        transaction.alternativePaymentMethod >> isv.sap.payment.enums.AlternativePaymentMethod.APY
        paymentTransactionService.getLatestTransaction(ALTERNATIVE_PAYMENT, order) >> of(transaction)
        paymentConfigurationService.getConfiguration(ALTERNATIVE_PAYMENT_CONFIG, _) >> configuration

        and:
        def statusMap = [(IsvAlternativePaymentStatus.SETTLED): OrderStatus.PAYMENT_CAPTURED]
        configuration.statusMap >> statusMap

        when:
        service.updateOrderByPaymentStatus(order, IsvAlternativePaymentStatus.SETTLED)

        then:
        1 * order.setStatus(OrderStatus.PAYMENT_CAPTURED)
        1 * modelService.save(order)
    }

    @Test
    def 'should not update order with payment status when no mapping exists'()
    {
        given:
        transaction.alternativePaymentMethod >> isv.sap.payment.enums.AlternativePaymentMethod.APY
        paymentTransactionService.getLatestTransaction(ALTERNATIVE_PAYMENT, order) >> of(transaction)
        paymentConfigurationService.getConfiguration(ALTERNATIVE_PAYMENT_CONFIG, _) >> configuration
        configuration.statusMap >> [:]

        when:
        service.updateOrderByPaymentStatus(order, IsvAlternativePaymentStatus.SETTLED)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message =~ 'No order status mapping exists for alternative payment status'
        0 * order.setStatus(_)
        0 * modelService.save(_)
    }

    def newTransactionEntry(PaymentTransactionType paymentTransactionType)
    {
        def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        entry.type >> paymentTransactionType
        entry.transactionStatus >> ACCEPT

        entry
    }
}
