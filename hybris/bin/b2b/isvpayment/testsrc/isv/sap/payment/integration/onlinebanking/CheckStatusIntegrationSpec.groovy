package isv.sap.payment.integration.onlinebanking

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CheckStatusIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CheckStatusRequestBuilder()

    @Test
    'Should receive accept for alipay initiate'()
    {
        given:
        def order = testCartUk()

        apTransactionCreator.addInitiate(order, method)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setPaymentTransactionType(isv.cjl.payment.enums.PaymentTransactionType.INITIATE)
                .addParam('alternatePaymentMethod', method)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.CHECK_STATUS
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

                apCheckStatusReplyPaymentStatus == 'COMPLETED'
                apCheckStatusReplyProcessorTransactionID != null
                apCheckStatusReplyReasonCode == '100'
                apCheckStatusReplyReconciliationID != null
            }
        }
        where:
        method                           | _
        AlternativePaymentMethod.APY | _
        AlternativePaymentMethod.AYM | _
    }

    @Test
    def 'Should receive accept for online banking sale'()
    {
        given:
        def order = testCartDe()

        String firstOptionId = apTransactionCreator.firstOptionId

        apTransactionCreator.addSale(order, AlternativePaymentMethod.valueOf(method.code), firstOptionId)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setPaymentTransactionType(isv.cjl.payment.enums.PaymentTransactionType.SALE)
                .addParam('alternatePaymentMethod', method)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.CHECK_STATUS
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

                apCheckStatusReplyPaymentStatus == status
                apCheckStatusReplyReasonCode == '100'
                apCheckStatusReplyReconciliationID != null
            }
        }
        where:
        method                           | status
        AlternativePaymentMethod.SOF | 'settled'
        AlternativePaymentMethod.IDL | 'pending'
        AlternativePaymentMethod.MCH | 'pending'
    }
}
