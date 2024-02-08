package isv.sap.payment.integration.paypal

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.paypal.CreateSessionRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class CreateSessionIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CreateSessionRequestBuilder()

    @Test
    'should receive accept'()
    {
        given:
        def order = testOrderUk()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setSuccessURL(testConfig.returnUrl)
                .setCancelURL(testConfig.cancelUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL.name()
            type == PaymentTransactionType.CREATE_SESSION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

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

                order.totalPrice == Double.parseDouble(amount)
                apSessionsReplyMerchantURL.contains('https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=')
            }
        }
    }
}
