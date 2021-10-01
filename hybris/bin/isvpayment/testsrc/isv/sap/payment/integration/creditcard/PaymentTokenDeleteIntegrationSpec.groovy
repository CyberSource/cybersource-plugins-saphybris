package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.PaymentTokenDeleteRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class PaymentTokenDeleteIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new PaymentTokenDeleteRequestBuilder()

    @Test
    "should get ACCEPT for payment token delete request"()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addPaymentToken(order)

        def subscription = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SUBSCRIPTION).get()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', subscription)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.DELETE_SUBSCRIPTION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'
            subscriptionID == null

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

                paySubscriptionDeleteReplyReasonCode != null
                paySubscriptionDeleteReplySubscriptionID != null
            }
        }
    }

    @Test
    'should get REJECT for wrong existing subscription'()
    {
        given:
        def card = testCard()
        def order = testOrderUk()

        ccTransactionCreator.addAuthorization(order, card)
        ccTransactionCreator.addPaymentToken(order)

        def subscription = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SUBSCRIPTION).get()
        subscription.subscriptionID = 'WRONG'
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', subscription)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.DELETE_SUBSCRIPTION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'
            subscriptionID == null

            code != null
            requestId != null
            currency == null
            requestToken != null

            time > operationStartTime
            time <= new Date()
        }
    }
}
