package isv.sap.payment.integration.onlinebanking

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.builder.alternative.InitiateRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class InitiateIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new InitiateRequestBuilder()

    @Test
    'should receive accept for web payment request'()
    {
        given:
        def order = testCartUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.APY)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setProductName(order.code)
                .setReturnUrl(testConfig.returnUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.INITIATE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

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
                apInitiateReplyReconciliationID != null
                merchantURL != null
            }
        }
    }

    @Test
    'should receive accept for mobile payment request'()
    {
        given:
        def order = testCartUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.AYM)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setProductName(order.code)
                .setReturnUrl(testConfig.returnUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.INITIATE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

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
                apInitiateReplyReconciliationID != null
                merchantURL != null
            }
        }
    }

    @Test
    'should receive error for wrong payment type'()
    {
        given:
        def order = testCartUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.IDL)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setProductName(order.code)
                .setReturnUrl(testConfig.returnUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.INITIATE
            transactionStatus == 'ERROR'
            transactionStatusDetails == '150'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                reasonCode == '150'
                decision == 'ERROR'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }
    }
}
