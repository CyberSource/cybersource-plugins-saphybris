package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.RefundFollowOnRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class RefundFollowOnIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new RefundFollowOnRequestBuilder()

    @Test
    'should receive Accept'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addCapture(order)

        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()
        def refundAmount = order.totalPrice
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .setAmount(refundAmount)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_FOLLOW_ON
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == order.totalPrice
            requestId != null
            requestToken != null
            currency.isocode == order.currency.isocode

            code.toString().contains(order.code)

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '100'
                decision == 'ACCEPT'
                purchaseTotalsCurrency == order.currency.isocode

                invalidField == '[]'
                missingField == '[]'

                ccCreditReplyRequestDateTime != null
                ccCreditReplyReconciliationID != null
                requestID != null
                requestToken != null
            }
        }
    }

    @Test
    'should receive Reject for not existing Capture'()
    {
        given: 'NOT existing order authorization'
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addCapture(order)

        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()
        capture.requestId = 'WRONG'
        def refundAmount = order.totalPrice
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .setAmount(refundAmount)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_FOLLOW_ON
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'
            requestId != null
            requestToken != null

            time > operationStartTime
            time <= new Date()

            properties.invalidField == '[*/c:captureRequestID]'
        }
    }
}
