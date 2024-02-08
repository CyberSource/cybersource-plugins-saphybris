package isv.sap.payment.integration.onlinebanking

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.alternative.RefundRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class RefundIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new RefundRequestBuilder()

    @Test
    'should receive accept for alipay'()
    {
        given:
        def order = testCartUk()

        apTransactionCreator.addInitiate(order, method)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(method)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', method)
                .setAmount(order.totalPrice)
                .setReason(reason)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == de.hybris.platform.payment.enums.PaymentTransactionType.REFUND
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

                apRefundReplyReasonCode == '100'
            }
        }
        where:
        method                           | reason
        AlternativePaymentMethod.APY | null
        AlternativePaymentMethod.AYM | 'reason'
    }

    @Test
    'should receive error for unpayed online bank transactions'()
    {
        given:
        def order = testCartUk()

        String firstOptionId = apTransactionCreator.firstOptionId

        apTransactionCreator.addSale(order, method, firstOptionId)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setApPaymentType(method)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', method)
                .setAmount(order.totalPrice)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == de.hybris.platform.payment.enums.PaymentTransactionType.REFUND
            transactionStatus == 'ERROR'
            transactionStatusDetails == '150'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == null
                reasonCode == '150'
                decision == 'ERROR'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apRefundReplyReasonCode == '150'
                apRefundReplyResponseCode != null
            }
        }
        where:
        method                           | _
        AlternativePaymentMethod.MCH | _
        AlternativePaymentMethod.IDL | _
    }

    @Test
    'should receive reject for unpayed sof transaction'()
    {
        given:
        def order = testCartDe()

        String firstOptionId = apTransactionCreator.firstOptionId

        apTransactionCreator.addSale(order, AlternativePaymentMethod.SOF, firstOptionId)

        when:
        def request = builder
                .setApPaymentType(AlternativePaymentMethod.SOF)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', AlternativePaymentMethod.SOF)
                .setAmount(order.totalPrice)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.REFUND
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            code.toString().contains(order.code)
            requestId != null

            with(properties) {
                merchantReferenceCode != null
                reasonCode == '100'
                decision == 'ACCEPT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apRefundReplyReasonCode == '100'
                apRefundReplyResponseCode != null
                apRefundReplyPaymentStatus == 'refunded'
            }
        }
    }

    @Test
    'should receive Accept for missing alipay initiate'()
    {
        given:
        def order = testCartUk()

        apTransactionCreator.addInitiate(order, method)
        def initiate = order.paymentTransactions.first().entries.first()
        initiate.requestId = 'WRONG'

        when:
        def request = builder
                .setApPaymentType(method)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', method)
                .setAmount(order.totalPrice)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == de.hybris.platform.payment.enums.PaymentTransactionType.REFUND
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            with(properties) {
                invalidField != '[]'
            }
        }
        where:
        method                           | paymentType
        AlternativePaymentMethod.APY | PaymentTransactionType.INITIATE
        AlternativePaymentMethod.AYM | PaymentTransactionType.INITIATE
    }
}
