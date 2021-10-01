package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.creditcard.RefundStandaloneRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class RefundStandaloneIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new RefundStandaloneRequestBuilder()

    @Test
    'should receive Accept for Order and Card'()
    {
        given:
        def order = testOrderUk()
        def cardInfo = testCard()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
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
    'should receive Accept for Order and Card for US order'()
    {
        given:
        def order = testOrderUs()
        def card = testCard()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', card)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
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
    'should receive accept for standalone refund request with correct subscription'()
    {
        given:
        def card = testCard()
        def oldOrder = testOrderUk()

        ccTransactionCreator.addAuthorization(oldOrder, card)
        String subscriptionId = ccTransactionCreator.getSubscription(oldOrder)
        def newOrder = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .setSubscriptionId(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == newOrder.totalPrice
            code.toString().contains(newOrder.code)
            requestId != null
            currency.isocode == newOrder.currency.isocode
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
            properties.merchantReferenceCode == newOrder.guid
        }
    }

    @Test
    'should receive reject for standalone refund request with wrong subscription'()
    {
        given:
        String subscriptionId = randomSubscriptionId()
        def order = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setSubscriptionId(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null
            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
        }
    }

    @Test
    'should receive reject when invalid field sent (wrong country code)'()
    {
        given: 'Order with wrong country code'
        def order = testOrderInvalidFields()
        def cardInfo = testCard()
        def operationStartTime = new Date()

        when: 'Request is sent'
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            code != null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            properties.invalidField == '[c:billTo/c:country]'
        }
    }

    @Test
    'should receive reject when invalid card'()
    {
        given:
        def order = testOrderUk()
        def cardInfo = wrongTestCard()
        def operationStartTime = new Date()

        when: 'Request is sent'
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.REFUND_STANDALONE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '231'

            code != null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()
        }
    }
}
