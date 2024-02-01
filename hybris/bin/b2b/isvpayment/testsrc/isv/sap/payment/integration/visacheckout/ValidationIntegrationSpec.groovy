package isv.sap.payment.integration.visacheckout

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.visacheckout.ValidateRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class ValidationIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new ValidateRequestBuilder()

    @Test
    "should receive reject for invalid visa checkout order id"()
    {
        given:
        def order = testOrderUk()
        def vcOrderId = testVisaCheckoutOrderId()
        def pares = testVisaCheckoutPARes()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setVcOrderId(vcOrderId)
                .setPARes(pares)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.VISA_CHECKOUT.name()
            type == PaymentTransactionType.VALIDATE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
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

                reasonCode == '102'
                decision == 'REJECT'
            }
        }
    }
}
