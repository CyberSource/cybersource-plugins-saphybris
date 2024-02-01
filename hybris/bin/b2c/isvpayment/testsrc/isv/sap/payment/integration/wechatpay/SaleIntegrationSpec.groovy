package isv.sap.payment.integration.wechatpay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class SaleIntegrationSpec extends IsvIntegrationSpec
{
    def 'should receive reject for WeChat sale request when using the amount that triggers refund transaction failures'()
    {
        given:
        def builder = new SaleRequestBuilder()
        builder.apPaymentType = AlternativePaymentMethod.WQR

        def cart = testCartUk(4000.01)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', cart)
                .addParam('invoiceHeaderMerchantDescriptor', 'some merchant')
                .addParam('apSaleServiceCancelURL', 'http://cancel')
                .addParam('apSaleServiceSuccessURL', 'http://success')
                .addParam('apSaleServiceTransactionTimeout', 100)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(cart.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == cart.guid
                reasonCode == '102'
                decision == 'REJECT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                responseCode == '10000'
                paymentStatus == 'failed'
                apSaleReply_reasonCode == '102'
            }
        }
    }

    def 'should receive accept for WeChat sale request'()
    {
        given:
        def builder = new SaleRequestBuilder()
        builder.apPaymentType = AlternativePaymentMethod.WQR

        def cart = testCartUk(8888.88)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', cart)
                .addParam('invoiceHeaderMerchantDescriptor', 'some merchant')
                .addParam('apSaleServiceCancelURL', 'http://cancel')
                .addParam('apSaleServiceSuccessURL', 'http://success')
                .addParam('apSaleServiceTransactionTimeout', 100)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(cart.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == cart.guid
                reasonCode == '100'
                decision == 'ACCEPT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                responseCode == '00001'
                paymentStatus == 'pending'
                reconciliationID != null
                apSaleReply_reasonCode == '100'
            }
        }
    }
}
