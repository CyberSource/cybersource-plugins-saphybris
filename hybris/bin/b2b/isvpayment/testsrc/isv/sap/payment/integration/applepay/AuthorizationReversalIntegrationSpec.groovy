package isv.sap.payment.integration.applepay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.applepay.AuthorizationReversalRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class AuthorizationReversalIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new AuthorizationReversalRequestBuilder()

    @Test
    "should receive accept for existing authorization"()
    {
        given:
        def order = testOrderUk()
        def card = testCard()

        def authorization = applePayTransactionsCreator.createAuthorization(order, card)

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.APPLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            requestId != null
            requestToken != null
            currency.isocode == order.currency.isocode

            amount == order.totalPrice

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '100'
                decision == 'ACCEPT'
                purchaseTotalsCurrency == order.currency.isocode

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }
    }

    @Test
    "should receive accept for not existing authorization"()
    {
        given:
        def order = testOrderUk()
        def card = testCard()

        def authorization = applePayTransactionsCreator.createAuthorization(order, card)
        authorization.requestId = 'WRONG'

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.APPLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            requestId != null
            requestToken != null

            with(properties) {
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[c:authRequestID]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }
    }
}
