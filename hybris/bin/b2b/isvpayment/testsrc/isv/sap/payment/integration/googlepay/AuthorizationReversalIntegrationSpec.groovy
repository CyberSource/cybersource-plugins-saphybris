package isv.sap.payment.integration.googlepay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.IgnoreIf

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.googlepay.AuthorizationReversalRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.paymentData

@ManualTest
@IgnoreIf({ paymentData.isEmpty() })
class AuthorizationReversalIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new AuthorizationReversalRequestBuilder()

    @Test
    'should receive accept for existing authorization'()
    {
        given:
        def order = testOrderUk()

        def authorization = googlePayTransactionsCreator.createAuthorization(order)

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.GOOGLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            requestId
            requestToken
            currency.isocode == order.currency.isocode

            amount == order.totalPrice

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '100'
                decision == 'ACCEPT'
                purchaseTotalsCurrency == order.currency.isocode

                invalidField == '[]'
                missingField == '[]'

                requestID
                requestToken
            }
        }
    }

    @Test
    'should receive reject for not existing authorization'()
    {
        given:
        def order = testOrderUk()

        def authorization = googlePayTransactionsCreator.createAuthorization(order)
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
            paymentTransaction.paymentProvider == PaymentType.GOOGLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            requestId
            requestToken

            with(properties) {
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[c:authRequestID]'
                missingField == '[]'

                requestID
                requestToken
            }
        }
    }
}
