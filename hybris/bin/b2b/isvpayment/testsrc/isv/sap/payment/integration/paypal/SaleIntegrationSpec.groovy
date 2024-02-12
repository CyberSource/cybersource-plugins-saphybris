package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypal.SaleRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class SaleIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new SaleRequestBuilder()

    @Test
    'should receive reject for invalid paypal order id'()
    {
        given:
        def order = testOrderUk()
        ppApTransactionCreator.addCreateSessionTransaction(order)
        ppApTransactionCreator.addCheckStatusTransaction(order)
        ppApTransactionCreator.addOrderSetup(order)
        def orderSetup = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.ORDER_SETUP).get()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', orderSetup)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'ERROR'
            transactionStatusDetails == '150'

            amount == order.totalPrice
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apSaleReplyReasonCode == '150'
                paymentStatus == 'FAILED'
            }
        }
    }
}
