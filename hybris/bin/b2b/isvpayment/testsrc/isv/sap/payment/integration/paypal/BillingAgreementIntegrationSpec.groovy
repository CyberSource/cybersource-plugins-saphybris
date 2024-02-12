package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.BillingAgreementRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class BillingAgreementIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new BillingAgreementRequestBuilder()

    @Test
    'should receive accept'()
    {
        given:
        def order = testOrderUk()
        ppApTransactionCreator.addCreateBillingAgreementSessionTransaction(order)
        def createSession = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_BILLING_AGREEMENT_SESSION).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', createSession)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.BILLING_AGREEMENT
            transactionStatus == 'REJECT'
            transactionStatusDetails == '233'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                apBillingAgreementReplyReasonCode == '233'
                merchantReferenceCode == order.guid

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }
    }
}
