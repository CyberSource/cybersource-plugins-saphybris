package isv.sap.payment.fulfilmentprocess.actions.order

import com.google.common.collect.Lists
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.user.UserModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.reports.transaction.detail.Report
import isv.cjl.payment.reports.transaction.detail.Request
import isv.cjl.payment.reports.transaction.detail.Requests
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

class ProcessDuplicatedAuthorizationActionSpec extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def user = Mock([useObjenesis: false], UserModel)

    def modelService = Mock([useObjenesis: false], ModelService)

    def paymentTransaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def duplicatePaymentTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def newPaymentTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def process = Mock([useObjenesis: false], OrderProcessModel)

    def paymentServiceExecutor = Mock([useObjenesis: false], PaymentServiceExecutor)

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    def report = Mock([useObjenesis: false], Report)

    def duplicatedAuthorizationAction = new ProcessDuplicatedAuthorizationAction()

    def paymentServiceResult = new PaymentServiceResult()

    void setup()
    {
        process.order >> order
        order.user >> user
        order.paymentTransactions >> Lists.newArrayList(paymentTransaction)

        paymentTransaction.entries >> Lists.newArrayList(duplicatePaymentTransactionEntry)
        paymentTransaction.paymentProvider >> PaymentType.CREDIT_CARD.name()
        duplicatePaymentTransactionEntry.type >> PaymentTransactionType.AUTHORIZATION
        duplicatePaymentTransactionEntry.paymentTransaction >> paymentTransaction

        paymentTransactionService.getTransaction(PaymentType.CREDIT_CARD, order) >> Optional.of(paymentTransaction)

        duplicatedAuthorizationAction.paymentServiceExecutor = paymentServiceExecutor
        duplicatedAuthorizationAction.modelService = modelService
    }

    @Test
    def 'Process duplicated transaction successfully'()
    {
        given:
        def requests = Mock([useObjenesis: false], Requests)
        requests.request >> Lists.newArrayList(new Request())
        report.requests >> requests
        newPaymentTransactionEntry.transactionStatus >> 'ACCEPT'

        duplicatePaymentTransactionEntry.transactionStatusDetails >> '104'
        paymentTransactionService.getLatestTransactionEntry(paymentTransaction,
                                                            PaymentTransactionType.AUTHORIZATION, IsvPaymentConstants.TransactionStatus.ERROR) >>
                Optional.of(duplicatePaymentTransactionEntry)
        paymentServiceResult.addData('transaction', newPaymentTransactionEntry)

        when:
        def result = duplicatedAuthorizationAction.executeAction(process)

        then:
        result == AbstractSimpleDecisionAction.Transition.OK

        1 * paymentServiceExecutor.execute(_ as PaymentServiceRequest) >> paymentServiceResult
    }

    @Test
    def 'Skip processing and return ok, no duplicated transaction found'()
    {
        given:
        duplicatePaymentTransactionEntry.transactionStatusDetails >> '100'
        paymentTransactionService.getLatestTransactionEntry(paymentTransaction,
                                                            PaymentTransactionType.AUTHORIZATION, IsvPaymentConstants.TransactionStatus.ERROR) >>
                Optional.empty()

        when:
        def result = duplicatedAuthorizationAction.executeAction(process)

        then:
        result == AbstractSimpleDecisionAction.Transition.OK
        0 * paymentServiceExecutor.execute(_ as PaymentServiceRequest)
    }
}
