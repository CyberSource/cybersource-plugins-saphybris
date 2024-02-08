package isv.sap.payment.integration.googlepay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.IgnoreIf

import isv.cjl.payment.service.executor.request.builder.googlepay.AuthorizationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.enums.PaymentType.GOOGLE_PAY
import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.fakePaymentData
import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.paymentData

@ManualTest
class AuthorizationIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new AuthorizationRequestBuilder()

    @Test
    'Should receive reject for fake payment token'()
    {
        given:
        def order = testOrderUk()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setPaymentData(fakePaymentData)
                .addParam(ORDER, order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == GOOGLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId
            currency == null

            with(properties) {
                invalidField == '[c:encryptedPayment/c:data]'
                missingField == '[]'

                requestID
                requestToken

                reasonCode == '102'
                decision == 'REJECT'
            }
        }
    }

    @Test
    @IgnoreIf({ paymentData.isEmpty() })
    'Should receive accept for valid payment data'()
    {
        given:
        def order = testOrderUk()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setPaymentData(paymentData)
                .addParam(ORDER, order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == GOOGLE_PAY.name()
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            code.toString().contains(order.code)
            requestId

            with(properties) {
                invalidField == '[]'
                missingField == '[]'

                requestID
                requestToken

                reasonCode == '100'
                decision == 'ACCEPT'
            }
        }
    }
}

