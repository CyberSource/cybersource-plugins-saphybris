package isv.sap.payment.integration.visacheckout

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.visacheckout.VoidRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class VoidIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new VoidRequestBuilder()

    @Test
    "should receive reject for invalid visa checkout order id"()
    {
        given:
        def order = testOrderUk()
        def vcOrderId = testVisaCheckoutOrderId()

        vcTransactionCreator.addAuthorization(order, vcOrderId)
        vcTransactionCreator.addCapture(order, vcOrderId)

        def capture = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CAPTURE).get()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', capture)
                .setVcOrderId(vcOrderId)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.VISA_CHECKOUT.name()
            type == PaymentTransactionType.VOID
            transactionStatus == 'REJECT'
            transactionStatusDetails == '246'

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
            }
        }
    }
}
