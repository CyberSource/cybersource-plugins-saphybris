package isv.sap.payment.service.alternativepayment.handler

import java.time.LocalDateTime
import java.time.ZoneId

import com.google.common.collect.Maps
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import jersey.repackaged.com.google.common.collect.ImmutableMap
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.enums.IsvAlternativePaymentStatus
import isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService

import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_CAPTURED
import static de.hybris.platform.core.enums.OrderStatus.REJECTED
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REJECT
import static isv.sap.payment.enums.AlternativePaymentMethod.APY
import static isv.sap.payment.enums.AlternativePaymentMethod.IDL
import static isv.sap.payment.enums.IsvAlternativePaymentStatus.SETTLED

@UnitTest
class AbstractAlternativePaymentPendingOrderHandlerSpec extends Specification
{
    def order = Mock(OrderModel)

    def paymentSerExecutor = Mock( PaymentServiceExecutor)

    def alternativePaymentOrderStatusService = Mock( AlternativePaymentOrderStatusService)

    def transaction = Mock( IsvPaymentTransactionModel)

    def configuration = Mock( IsvCheckAlternativePaymentStatusConfigurationModel)

    def transactionEntry = Mock( IsvPaymentTransactionEntryModel)

    def paymentServiceRequest = PaymentServiceRequest.create()

    @SuppressWarnings('BracesForClass')
    def alternativePaymentPendingOrderHandler = new AbstractAlternativePaymentPendingOrderHandler() {
        @SuppressWarnings('UnusedMethodParameter')
        @Override
        protected PaymentServiceRequest createCheckStatusRequestBuilder(final AbstractOrderModel order, final IsvPaymentTransactionModel transaction, final IsvPaymentTransactionEntryModel transactionEntry)
        {
            paymentServiceRequest
        }
    }

    void setup()
    {
        alternativePaymentPendingOrderHandler.setAlternativePaymentOrderStatusService(alternativePaymentOrderStatusService)
        alternativePaymentPendingOrderHandler.setPaymentServiceExecutor(paymentSerExecutor)

        alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order) >> transaction
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
        alternativePaymentOrderStatusService.getAlternativePaymentTransactionEntry(transaction) >> transactionEntry
    }

    @Test
    def 'process order on pending payment status and update status to complete'()
    {
        given:
        transaction.alternativePaymentMethod >> IDL
        transactionEntry = newTransactionEntry(PaymentTransactionType.SALE)
        transaction.entries >> [transactionEntry]
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
        transactionEntry.transactionStatus >> ACCEPT
        transactionEntry.properties >> ['apCheckStatusReplyPaymentStatus': 'SETTLED']

        configuration.statusMap >> ImmutableMap.of(SETTLED, PAYMENT_CAPTURED)

        order.creationtime >> Date.from(LocalDateTime.now().minusMinutes(15).atZone(ZoneId.systemDefault()).toInstant())

        PaymentServiceResult result = PaymentServiceResult.create()
        result.addData(TRANSACTION, transactionEntry)

        paymentSerExecutor.execute(paymentServiceRequest) >> result

        when:
        alternativePaymentPendingOrderHandler.handle(order)

        then:
        1 * alternativePaymentOrderStatusService.updateOrderByPaymentStatus(order, SETTLED)
    }

    @Test
    def 'process order on pending payment status skip update of the status when no status returned'()
    {
        given:
        transaction.alternativePaymentMethod >> IDL
        transactionEntry = newTransactionEntry(PaymentTransactionType.SALE)
        transaction.entries >> [transactionEntry]
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
        transactionEntry.transactionStatus >> ACCEPT
        transactionEntry.properties >> ['apCheckStatusReplyPaymentStatus': '']

        configuration.statusMap >> ImmutableMap.of(SETTLED, PAYMENT_CAPTURED)

        order.creationtime >> Date.from(LocalDateTime.now().minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant())

        PaymentServiceResult result = PaymentServiceResult.create()
        result.addData(TRANSACTION, transactionEntry)

        paymentSerExecutor.execute(paymentServiceRequest) >> result

        when:
        alternativePaymentPendingOrderHandler.handle(order)

        then:
        0 * alternativePaymentOrderStatusService.updateOrderByPaymentStatus(_ as OrderModel, _ as IsvAlternativePaymentStatus)
    }

    @Test
    def 'process order having rejected payments'()
    {
        given:
        transaction.alternativePaymentMethod >> APY
        transactionEntry = Mock( IsvPaymentTransactionEntryModel)
        transaction.entries >> [transactionEntry]
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
        transactionEntry.transactionStatus >> REJECT
        transactionEntry.properties >> Maps.newHashMap()

        configuration.statusMap >> ImmutableMap.of(REJECT, REJECTED)

        order.creationtime >> Date.from(LocalDateTime.now().minusMinutes(120).atZone(ZoneId.systemDefault()).toInstant())

        PaymentServiceResult result = PaymentServiceResult.create()
        result.addData(TRANSACTION, transactionEntry)

        paymentSerExecutor.execute(paymentServiceRequest) >> result

        when:
        alternativePaymentPendingOrderHandler.handle(order)

        then:
        1 * alternativePaymentOrderStatusService.updateOrderByPaymentStatus(order, IsvAlternativePaymentStatus.REJECTED)
    }

    def newTransactionEntry(PaymentTransactionType paymentTransactionType)
    {
        def entry = Mock( IsvPaymentTransactionEntryModel)
        entry.type >> paymentTransactionType
        entry.transactionStatus >> ACCEPT

        entry
    }
}
