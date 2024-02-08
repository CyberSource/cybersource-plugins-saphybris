package isv.sap.payment.integration.paypalso

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypalso.SetRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class SetIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new SetRequestBuilder()

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
                .setReturnURL(testConfig.returnUrl)
                .setCancelURL(testConfig.cancelUrl)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL_SO.name()
            type == PaymentTransactionType.SET
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == order.totalPrice
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

                payPalEcSetReplyPaypalToken != null
                payPalEcSetReplyReasonCode == '100'
                payPalEcSetReplyCorrelationID != null
            }
        }
    }
}
