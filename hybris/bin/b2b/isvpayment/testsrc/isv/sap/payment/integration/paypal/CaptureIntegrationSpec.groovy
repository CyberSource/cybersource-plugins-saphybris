package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.CaptureRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CaptureIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CaptureRequestBuilder()

    @Test
    'should receive reject for invalid paypal order id'()
    {
        given:
        def order = testOrderUk()
        ppApTransactionCreator.addCreateSessionTransaction(order)
        ppApTransactionCreator.addCheckStatusTransaction(order)
        ppApTransactionCreator.addOrderSetup(order)
        ppApTransactionCreator.addAuthorization(order)
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.CAPTURE
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

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apCaptureReplyReasonCode == '102'
            }
        }
    }
}
