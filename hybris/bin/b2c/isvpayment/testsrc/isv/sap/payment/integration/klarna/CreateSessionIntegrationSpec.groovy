package isv.sap.payment.integration.klarna

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.alternative.CreateSessionRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.integration.helpers.TestConfig.MERCHANT_REGION_EUROPE
import static isv.sap.payment.integration.helpers.TestConfig.MERCHANT_REGION_US

@ManualTest
class CreateSessionIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CreateSessionRequestBuilder()

    @Test
    'Should create Klarna session without exposing Delivery and Payment Address'()
    {
        given:
        switchMerchantConfig(merchantRegion)
        def cart = testCartWithoutSensitivePersonalData(country)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', cart)
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.CREATE_SESSION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(cart.code)
            requestId != null
            currency.isocode == cart.currency.isocode

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == cart.guid
                purchaseTotalsCurrency == cart.currency.isocode

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
                apSessionsReplyProcessorToken != null

                apSessionsReplyReasonCode == '100'
                apSessionsReplyResponseCode != null
                reasonCode == '100'
                decision == 'ACCEPT'
            }
        }

        where:
        country | merchantRegion
        'UK'    | MERCHANT_REGION_EUROPE
        'DE'    | MERCHANT_REGION_EUROPE
        'US'    | MERCHANT_REGION_US
    }
}
