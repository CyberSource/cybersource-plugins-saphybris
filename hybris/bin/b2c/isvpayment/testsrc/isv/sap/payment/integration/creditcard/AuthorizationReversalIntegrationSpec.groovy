package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationReversalRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class AuthorizationReversalIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new AuthorizationReversalRequestBuilder()

    @Test
    'should receive Accept'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)

        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()
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
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
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

                requestID != null
                requestToken != null

                ccAuthReversalReplyReasonCode == '100'
                ccAuthReversalReplyProcessorResponse != null
                ccAuthReversalReplyRequestDateTime != null
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
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
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
