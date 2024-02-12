package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.CheckStatusRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CheckStatusIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CheckStatusRequestBuilder()

    @Test
    'should receive accept'()
    {
        given:
        def order = testOrderUk()
        ppApTransactionCreator.addCreateSessionTransaction(order)
        def setTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SESSION).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', setTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
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

                apCheckStatusReplyReasonCode == '100'
                apCheckStatusReplyPaymentStatus == 'CREATED'
            }
        }
    }
}
