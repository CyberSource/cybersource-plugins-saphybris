package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.PaymentTokenCreateRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class PaymentTokenCreateIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new PaymentTokenCreateRequestBuilder()

    @Test
    'should get ACCEPT for payment token creation request'()
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
            type == PaymentTransactionType.CREATE_SUBSCRIPTION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'
            subscriptionID != null

            code.toString().contains(order.code)
            requestId != null
            currency == null
            requestToken != null

            time > operationStartTime
            time <= new Date()
            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '100'
                decision == 'ACCEPT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                paySubscriptionCreateReplyReasonCode != null
                paySubscriptionCreateReplySubscriptionID != null
            }
        }
    }

    @Test
    'should get REJECT for wrong Auth Request Results'()
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
            type == PaymentTransactionType.CREATE_SUBSCRIPTION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'
            subscriptionID == null

            code != null
            requestId != null
            currency == null
            requestToken != null

            time > operationStartTime
            time <= new Date()

            properties.invalidField == '[c:paySubscriptionCreateService/c:paymentRequestID]'
        }
    }
}
