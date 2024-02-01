package isv.sap.payment.integration.visacheckout

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.visacheckout.GetRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class GetIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new GetRequestBuilder()

    @Test
    'Should not get data from Visa Checkout with invalid VCO Order Id'()
    {
        given:
        def order = testOrderUk()
        def vcOrderId = testVisaCheckoutOrderId()

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
            type == PaymentTransactionType.GET
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            with(properties) {
                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                getVisaCheckoutDataReplyReasonCode == '102'
                reasonCode == '102'
                decision == 'REJECT'
            }
        }
    }
}
