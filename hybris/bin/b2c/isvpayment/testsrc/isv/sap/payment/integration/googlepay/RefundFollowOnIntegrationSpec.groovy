package isv.sap.payment.integration.googlepay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.IgnoreIf

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.googlepay.RefundFollowOnRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.paymentData

@ManualTest
@IgnoreIf({ paymentData.isEmpty() })
class RefundFollowOnIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new RefundFollowOnRequestBuilder()

    @Test
    'should receive accept for existing capture'()
    {
        given:
        def order = testOrderUk()

        def authorization = googlePayTransactionsCreator.createAuthorization(order)
        def capture = googlePayTransactionsCreator.createCapture(order, authorization)

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .addParam('amount', order.totalPrice.toBigDecimal())
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.GOOGLE_PAY.name()
            type == PaymentTransactionType.REFUND_FOLLOW_ON
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
    'should receive reject for not existing capture'()
    {
        given:
        def order = testOrderUk()

        def authorization = googlePayTransactionsCreator.createAuthorization(order)
        def capture = googlePayTransactionsCreator.createCapture(order, authorization)
        capture.requestId = 'WRONG'

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .addParam('amount', order.totalPrice.toBigDecimal())
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.GOOGLE_PAY.name()
            type == PaymentTransactionType.REFUND_FOLLOW_ON
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            requestId
            requestToken

            with(properties) {
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[*/c:captureRequestID]'
                missingField == '[]'

                requestID
                requestToken
            }
        }
    }
}
