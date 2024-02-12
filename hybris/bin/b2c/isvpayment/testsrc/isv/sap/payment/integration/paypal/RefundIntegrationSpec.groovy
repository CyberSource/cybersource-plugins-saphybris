package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.RefundRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class RefundIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new RefundRequestBuilder()

    @Test
    'should receive reject for invalid paypal order id'()
    {
        given:
        def order = testOrderUk()
        ppApTransactionCreator.addCreateSessionTransaction(order)
        ppApTransactionCreator.addCheckStatusTransaction(order)
        ppApTransactionCreator.addOrderSetup(order)
        ppApTransactionCreator.addAuthorization(order)
        ppApTransactionCreator.addCapture(order)
        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()

        def operationStartTime = new Date()

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
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.REFUND
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                apRefundReplyReasonCode == '102'

                invalidField == '[c:apRefundService/c:refundRequestID]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }
    }
}
