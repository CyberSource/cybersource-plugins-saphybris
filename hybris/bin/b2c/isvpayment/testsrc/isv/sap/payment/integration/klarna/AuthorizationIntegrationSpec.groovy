package isv.sap.payment.integration.klarna

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.alternative.AuthorizationRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

/**
 * Created by esanchez on 9/5/17.
 */

@ManualTest
class AuthorizationIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new AuthorizationRequestBuilder()

    @Test
    'Should not authorize Klarna for non-existing preauthorization token'()
    {
        given:
        def cart = testOrderUk()
        def preApprovalToken = testKlarnaPreauthorizationToken()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', cart)
                .setPreapprovalToken(preApprovalToken)
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .setBillToLanguage(locale)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(cart.code)
            requestId != null
            currency == null

            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == cart.guid

                invalidField == '[c:apAuthService/c:preapprovalToken]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apAuthReply_reasonCode == '102'
                responseCode != null
                paymentStatus == 'failed'
                reasonCode == '102'
                decision == 'REJECT'
            }
        }

        where:
        locale  | _
        'en-GB' | _
        'de-DE' | _
    }
}
