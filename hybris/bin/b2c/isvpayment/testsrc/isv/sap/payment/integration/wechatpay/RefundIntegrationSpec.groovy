package isv.sap.payment.integration.wechatpay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.alternative.RefundRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.enums.AlternativePaymentMethod.WQR

@ManualTest
class RefundIntegrationSpec extends IsvIntegrationSpec
{
    def 'should refund succesfully WeChat orders'()
    {
        given:
        def cart = testCartUk()
        apTransactionCreator.addSale(cart, WQR)
        apTransactionCreator.addCheckStatus(cart, PaymentTransactionType.SALE, WQR)

        def checkStatusTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                cart.paymentTransactions.first(), PaymentTransactionType.SALE).get()

        def operationStartTime = new Date()

        when:
        def request = new RefundRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('alternatePaymentMethod', WQR)
                .addParam('order', cart)
                .addParam('transaction', checkStatusTransaction)
                .addParam('amount', cart.totalPrice)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.REFUND
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == cart.totalPrice
            code.toString().contains(cart.code)
            requestId != null
            currency.isocode == cart.currency.isocode

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == cart.guid
                reasonCode == '100'
                decision == 'ACCEPT'
                invalidField == '[]'
                missingField == '[]'
            }
        }
    }

    def 'should fail to refund WeChat orders when using the amount that triggers failures'()
    {
        given:
        def cart = testCartUk()
        apTransactionCreator.addSale(cart, WQR)
        apTransactionCreator.addCheckStatus(cart, PaymentTransactionType.SALE, WQR)

        def checkStatusTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                cart.paymentTransactions.first(), PaymentTransactionType.SALE).get()

        def operationStartTime = new Date()

        when: 'When using the payment simulator, 2000.07 is the trigger for failed refunds'
        def request = new RefundRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('alternatePaymentMethod', WQR)
                .addParam('order', cart)
                .addParam('transaction', checkStatusTransaction)
                .addParam('amount', 2000.07)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.REFUND
            transactionStatus == 'ERROR'
            transactionStatusDetails == '150'

            amount == null
            code.toString().contains(cart.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                reasonCode == '150'
                decision == 'ERROR'
                apRefundReplyPaymentStatus == 'failed'
                invalidField == '[]'
                missingField == '[]'
            }
        }
    }
}
