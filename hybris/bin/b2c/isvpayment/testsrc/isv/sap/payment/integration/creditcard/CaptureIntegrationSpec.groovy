package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.CaptureRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CaptureIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new CaptureRequestBuilder()

    @Test
    'should receive Accept'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()
        ccTransactionCreator.addAuthorization(order, card)
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                corder.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.CAPTURE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            requestId != null
            requestToken != null
            currency.isocode == order.currency.isocode

            amount == order.totalPrice

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

                ccCaptureReplyReconciliationID != null
                requestID != null
                requestToken != null
            }
        }
    }

    @Test
    'should receive Reject for not existing authorisation'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()
        ccTransactionCreator.addAuthorization(order, card)
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()

        authorization.requestId = 'WRONG'
        authorization.properties.requestID = 'WRONG'
        authorization.paymentTransaction.requestId = 'WRONG'
        authorization.requestToken = 'WRONG'

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.CAPTURE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'
            requestId != null
            requestToken != null

            time > operationStartTime
            time <= new Date()

            properties.invalidField == '[c:authRequestID]'
        }
    }
}
