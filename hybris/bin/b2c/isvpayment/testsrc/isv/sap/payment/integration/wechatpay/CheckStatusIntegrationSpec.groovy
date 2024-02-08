package isv.sap.payment.integration.wechatpay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CheckStatusIntegrationSpec extends IsvIntegrationSpec
{
    def 'should receive the expected payment status for WeChat check status requests'()
    {
        given:
        def builder = new CheckStatusRequestBuilder()
        def cart = testCartUk()
        apTransactionCreator.addSale(cart, AlternativePaymentMethod.WQR)

        def saleTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                cart.paymentTransactions.first(), PaymentTransactionType.SALE).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setReconciliationId(reconciliationId)
                .addParam('order', cart)
                .addParam('transaction', saleTransaction)
                .addParam('alternatePaymentMethod', AlternativePaymentMethod.WQR)
                .addParam('paymentTransactionType', isv.cjl.payment.enums.PaymentTransactionType.SALE)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.CHECK_STATUS
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
                apCheckStatusReplyPaymentStatus == checkStatusReply
                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }

        where:
        reconciliationId | checkStatusReply
        'TC200000'       | 'pending'
        ''               | 'settled'
        'TC200001'       | 'abandoned'
        'TC200010'       | 'failed'
    }
}
