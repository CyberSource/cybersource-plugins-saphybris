package isv.sap.payment.integration.klarna

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.alternative.UpdateSessionRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.integration.helpers.TestConfig.MERCHANT_REGION_EUROPE
import static isv.sap.payment.integration.helpers.TestConfig.MERCHANT_REGION_US

@ManualTest
class UpdateSessionIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new UpdateSessionRequestBuilder()

    @Test
    'Should accept Klarna Update Session request with Delivery and Billing info'()
    {
        given:
        switchMerchantConfig(merchantRegion)
        def order = testOrderFor(country)
        apTransactionCreator.createKlarnaSession(order)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .setBillToLanguage('en-GB')
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.UPDATE_SESSION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency.isocode == order.currency.isocode

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                purchaseTotalsCurrency == order.currency.isocode

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apSessionsReplyReasonCode == '100'
                apSessionsReplyResponseCode == '00000'
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

    @Test
    'Should reject Klarna Update session request for en-UK language'()
    {
        given:
        def order = testOrderUk()
        apTransactionCreator.createKlarnaSession(order)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .setBillToLanguage('en-UK')
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.UPDATE_SESSION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid

                invalidField == '[c:billTo/c:language]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apSessionsReplyReasonCode == '102'
                reasonCode == '102'
                decision == 'REJECT'
            }
        }
    }
}
