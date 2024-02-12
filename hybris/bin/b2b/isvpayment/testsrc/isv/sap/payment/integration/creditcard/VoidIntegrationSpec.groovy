package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.VoidRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class VoidIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new VoidRequestBuilder()

    @Test
    'should receive Accept for voiding Capture'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addCapture(order)

        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.VOID
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
                requestID != null
                requestToken != null

                invalidField == '[]'
                missingField == '[]'

                voidReplyReasonCode == '100'
                voidReplyCurrency != null
                voidReplyRequestDateTime != null
            }
        }
    }

    @Test
    'should receive Accept for voiding Refund Standalone'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addRefundStandalone(order, card)

        def refund = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.REFUND_STANDALONE).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', refund)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.VOID
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
                requestID != null
                requestToken != null

                invalidField == '[]'
                missingField == '[]'

                voidReplyReasonCode == '100'
                voidReplyCurrency != null
                voidReplyRequestDateTime != null
            }
        }
    }

    @Test
    'should receive Reject for not existing capture'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addCapture(order)

        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()
        capture.requestId = 'WRONG'
        capture.properties.requestID = 'WRONG'
        capture.paymentTransaction.requestId = 'WRONG'
        capture.requestToken = 'WRONG'
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.VOID
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'
            requestId != null
            requestToken != null

            time > operationStartTime
            time <= new Date()

            properties.invalidField == '[c:voidService/c:voidRequestID]'
        }
    }
}
