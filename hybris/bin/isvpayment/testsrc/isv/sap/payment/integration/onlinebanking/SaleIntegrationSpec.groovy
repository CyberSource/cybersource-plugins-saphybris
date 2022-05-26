package isv.sap.payment.integration.onlinebanking

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class SaleIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new SaleRequestBuilder()
    def descriptor = 'test'

    @Test
    'should receive accept for ideal request without payment option'()
    {
        given:
        def operationStartTime = new Date()
        def order = testCartUk()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.IDL)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.cancelUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
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

                processorTransactionID != null
                merchantURL.contains(processorTransactionID)
                responseCode == '00001'
                paymentStatus == 'pending'
                reconciliationID != null
                apSaleReply_reasonCode == '100'
            }
        }
    }

    @Test
    'should receive accept for ideal request with option'()
    {
        given:
        def operationStartTime = new Date()
        def order = testCartUk()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.IDL)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.cancelUrl)
                .setPaymentOptionID(testConfig.idealOption)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
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

                apSaleReply_reasonCode == '100'
                responseCode == '00001'
                paymentStatus == 'pending'
                merchantURL != null
                reconciliationID != null
                processorTransactionID != null
            }
        }
    }

    @Test
    'should receive reject for ideal request with wrong option'()
    {
        given:
        def operationStartTime = new Date()
        def order = testCartUk()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.IDL)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.cancelUrl)
                .setPaymentOptionID('abc')
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[c:apSaleService/c:paymentOptionID]'
                missingField == '[]'

                requestID != null
                requestToken != null

                paymentStatus == 'failed'
            }
        }
    }

    @Test
    'should receive accept for bancontact request'()
    {
        given:
        def order = testCartUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.MCH)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.cancelUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == de.hybris.platform.payment.enums.PaymentTransactionType.SALE
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

                processorTransactionID != null
                merchantURL != null
                responseCode == '00001'
                paymentStatus == 'pending'
                reconciliationID != null
                apSaleReply_reasonCode == '100'
            }
        }
    }

    @Test
    'should receive accept for sofort request'()
    {
        given:
        def order = testCartDe()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.SOF)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.cancelUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == de.hybris.platform.payment.enums.PaymentTransactionType.SALE
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

                processorTransactionID != null
                merchantURL != null
                responseCode == '00001'
                paymentStatus == 'pending'
                reconciliationID != null
                apSaleReply_reasonCode == '100'
            }
        }
    }

    @Test
    'should receive reject for wrong payment type'()
    {
        given:
        def order = testCartUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.AYM)
                .setMerchantId(testConfig.merchant)
                .setMerchantDescriptor(descriptor)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setCancelURL(testConfig.returnUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apSaleReply_reasonCode == '102'
            }
        }
    }
}
