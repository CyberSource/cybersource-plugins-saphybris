package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.servicelayer.config.ConfigurationService
import org.apache.commons.configuration.Configuration
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.AlternativePaymentStatus
import isv.cjl.payment.enums.CheckStatusDecision
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.sap.payment.addon.enums.CheckStatusResponse
import isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService

import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.CHECK_STATUS_RECONCILIATION_ID
import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsResponseFields.PAYMENT_STATUS

@UnitTest
class AlternativePaymentStatusFacadeImplSpec extends Specification
{
    def alternativePaymentOrderStatusService = Mock(AlternativePaymentOrderStatusService)
    def paymentServiceExecutor = Mock(PaymentServiceExecutor)
    def configurationService = Mock(ConfigurationService)
    def facade = new AlternativePaymentStatusFacadeImpl(
            alternativePaymentOrderStatusService: alternativePaymentOrderStatusService,
            paymentServiceExecutor: paymentServiceExecutor,
            configurationService: configurationService
    )

    def transaction = Mock(IsvPaymentTransactionModel)
    def transactionEntry = Mock(IsvPaymentTransactionEntryModel)
    def order = Mock(AbstractOrderModel)

    @Test
    @Unroll
    def 'Should resolve payment check status response when able to check payment status'()
    {
        given:
        def merchantId = 'testMerchant'
        def paymentMethod = AlternativePaymentMethod.WQR
        def paymentServiceResult = Mock(PaymentServiceResult)
        def configuration = Mock(Configuration)

        configurationService.configuration >> configuration
        configuration.getString(CHECK_STATUS_RECONCILIATION_ID) >> 'DUMMY_RECONCILIATION_ID'
        transaction.merchantId >> merchantId
        transaction.alternativePaymentMethod >> paymentMethod
        transactionEntry.type >> PaymentTransactionType.CHECK_STATUS
        transactionEntry.transactionStatus >> TransactionStatus.ACCEPT
        transactionEntry.properties >> [(PAYMENT_STATUS): paymentTransactionStatus]

        when:
        def response = facade.resolveCheckStatusResponse(order)

        then:
        1 * alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order) >> transaction
        1 * alternativePaymentOrderStatusService.getAlternativePaymentTransactionEntry(transaction) >> transactionEntry
        1 * alternativePaymentOrderStatusService.shouldRunCheckStatus(transaction) >> CheckStatusDecision.RUN
        1 * paymentServiceResult.getData(PaymentConstants.CommonFields.TRANSACTION) >> transactionEntry
        1 * paymentServiceExecutor.execute {
            it.requestParams.order == order
            it.requestParams.transaction == transactionEntry
            it.requestParams.alternatePaymentMethod.code == paymentMethod.code
            it.requestParams.paymentTransactionType.code == PaymentTransactionType.CHECK_STATUS.code
        } >> paymentServiceResult

        response == checkStatusResponse

        where:
        paymentTransactionStatus                  | checkStatusResponse
        AlternativePaymentStatus.PENDING.code | CheckStatusResponse.PAYMENT_PENDING
        AlternativePaymentStatus.SETTLED.code | CheckStatusResponse.PAYMENT_SUCCESS
    }

    @Test
    @Unroll
    def 'Should resolve payment check status response when unable to check payment status'()
    {
        when:
        def response = facade.resolveCheckStatusResponse(order)

        then:
        1 * alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order) >> transaction
        1 * alternativePaymentOrderStatusService.shouldRunCheckStatus(transaction) >> checkStatusDecision

        response == checkStatusResponse

        where:
        checkStatusDecision                   | checkStatusResponse
        CheckStatusDecision.ATTEMPTS_EXCEEDED | CheckStatusResponse.CHECK_STATUS_TOO_MANY_ATTEMPTS
        CheckStatusDecision.SKIP              | CheckStatusResponse.PAYMENT_PENDING
        CheckStatusDecision.ERROR             | CheckStatusResponse.CHECK_STATUS_ERROR
    }
}
