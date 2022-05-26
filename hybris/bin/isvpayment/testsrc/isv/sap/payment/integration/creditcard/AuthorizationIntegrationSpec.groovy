package isv.sap.payment.integration.creditcard

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec
import org.junit.Test

import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.FLEX_TOKEN

@ManualTest
class AuthorizationIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new AuthorizationRequestBuilder()

    @Test
    'should receive accept'()
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
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == order.totalPrice
            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

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

                purchaseTotalsCurrency == order.currency.isocode
                decisionReplyCasePriority != null

                ccAuthReplyReasonCode == '100'
                ccAuthReplyProcessorResponse != null
                ccAuthReplyAvsCode != null
                ccAuthReplyAuthorizedDateTime != null
                ccAuthReplyAuthorizationCode != null
            }
        }
    }

    @Test
    'should receive accept for US order'()
    {
        given:
        def order = testOrderUs()
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
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == order.totalPrice
            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

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

                purchaseTotalsCurrency == order.currency.isocode
                decisionReplyCasePriority != null
            }
        }
    }

    @Test
    'should receive review'()
    {
        given:
        def order = testCartForReview()
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
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REVIEW'
            transactionStatusDetails == '480'

            amount == order.totalPrice
            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '480'
                decision == 'REVIEW'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                purchaseTotalsCurrency == order.currency.isocode
                decisionReplyCasePriority != null
            }
        }
    }

    @Test
    'should not receive review with DM off'()
    {
        given:
        def order = testCartForReview()
        def cardInfo = testCard()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .addParam('decisionManagerEnabled', false)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == order.totalPrice
            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

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

                purchaseTotalsCurrency == order.currency.isocode
            }
        }
    }

    @Test
    'should receive reject when invalid field sent (wrong country code)'()
    {
        given:
        def order = testCartInvalidFields()
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
            type == PaymentTransactionType.AUTHORIZATION
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

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '231'

            code != null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()
            properties.merchantReferenceCode == order.guid
        }
    }

    @Test
    'should receive reject when required field is missing (state)'()
    {
        given:
        def order = testCartMissingFields()
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
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '101'

            code != null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            properties.missingField == '[c:billTo/c:state]'
        }
    }

    @Test
    'should receive error'()
    {
        given:
        def order = testCartWithErrorCode()
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
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'ERROR'
            transactionStatusDetails != null

            code != null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            properties.ccAuthReplyProcessorResponse != null
        }
    }

    @Test
    'should receive reject for auth request with incorrect flex token'()
    {
        given:
        def card = testCard()
        def oldOrder = testOrderUk()

        ccTransactionCreator.addAuthorization(oldOrder, card)
        def newOrder = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .addParam(FLEX_TOKEN, 'INVALID_FLEX_TOKEN')
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(newOrder.code)
            requestId != null
            currency == null
            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
        }
    }

    @Test
    'should receive reject for auth request with incorrect flex token and correct card info'()
    {
        given:
        def cardInfo = testCard()
        def oldOrder = testOrderUk()
        ccTransactionCreator.addAuthorization(oldOrder, cardInfo)

        def newOrder = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .addParam('card', cardInfo)
                .addParam(FLEX_TOKEN, 'INVALID_FLEX_TOKEN')
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(newOrder.code)
            requestId != null
            currency == null
            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
        }
    }

    @Test
    'should throw exception when no credit card or flex token is provided'()
    {
        given:
        def newOrder = testOrderUk()
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .build()

        when:
        paymentServiceExecutor.execute(request)

        then:
        RuntimeException exception = thrown()
        exception.message == 'one of card info ,flex transient token or subscription ID must be supplied'
    }

    @Test
    'should receive accept for auth request with correct subscription'()
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
                .setSubscriptionID(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
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
    'should receive accept for auth request with correct subscription and card'()
    {
        given:
        def cardInfo = testCard()
        def oldOrder = testOrderUk()
        ccTransactionCreator.addAuthorization(oldOrder, cardInfo)

        String subscriptionId = ccTransactionCreator.getSubscription(oldOrder)
        def newOrder = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .addParam('card', cardInfo)
                .setSubscriptionID(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
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
    'should receive accept for auth request with correct subscription and no card info '()
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
                .addParam('card', null)
                .addParam('flexToken', null)
                .setSubscriptionID(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
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
    'should receive reject for auth request with wrong subscription'()
    {
        given:
        String subscriptionId = randomSubscriptionId()
        def newOrder = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .setSubscriptionID(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(newOrder.code)
            requestId != null
            currency == null
            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
        }
    }

    @Test
    'should receive reject for auth request with wrong subscription and correct card'()
    {
        given:
        String subscriptionId = randomSubscriptionId()
        def newOrder = testOrderUk()
        def card = testCard()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', newOrder)
                .addParam('card', card)
                .addParam('flexToken', null)
                .setSubscriptionID(subscriptionId)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(newOrder.code)
            requestId != null
            currency == null
            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()
        }
    }
}
