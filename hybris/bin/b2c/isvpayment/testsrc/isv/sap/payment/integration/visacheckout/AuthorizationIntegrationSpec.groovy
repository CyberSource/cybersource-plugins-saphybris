package isv.sap.payment.integration.visacheckout

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.visacheckout.AuthorizationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class AuthorizationIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new AuthorizationRequestBuilder()

    @Test
    'Should receive reject for invalid visa checkout order id'()
    {
        given:
        def order = testOrderUk()
        def vcOrderId = testVisaCheckoutOrderId()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setVcOrderId(vcOrderId)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.VISA_CHECKOUT.name()
            type == PaymentTransactionType.AUTHORIZATION
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
