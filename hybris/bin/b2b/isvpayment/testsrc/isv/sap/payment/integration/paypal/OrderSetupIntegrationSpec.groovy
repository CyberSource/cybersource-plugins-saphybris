package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.OrderSetupRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class OrderSetupIntegrationSpec extends IsvIntegrationSpec
{

    static final FAKE_ID = 'FAKE'
    def builder = new OrderSetupRequestBuilder()

    @Test
    'should receive reject for not authorized payment request'()
    {
        given:
        def order = testCartUk()
        ppApTransactionCreator.addCreateSessionTransaction(order)
        def sessionTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SESSION).get()
        ppApTransactionCreator.addCheckStatusTransaction(order)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('PayerID', FAKE_ID)
                .addParam('transaction', sessionTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.ORDER_SETUP
            transactionStatus == 'REJECT'
            transactionStatusDetails == '223'

            amount == order.totalPrice
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
