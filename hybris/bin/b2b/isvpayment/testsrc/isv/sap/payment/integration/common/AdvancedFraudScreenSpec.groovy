package isv.sap.payment.integration.common

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.fraud.AdvancedFraudScreenRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class AdvancedFraudScreenSpec extends IsvIntegrationSpec
{

    def builder = new AdvancedFraudScreenRequestBuilder()

    @Test
    'should receive accept by enabled decision manager'()
    {
        given:
        def order = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setDecisionManagerEnabled(true)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.ADVANCED_FRAUD_SCREEN
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

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

                afsReplyReasonCode == '100'
                afsReplyAfsResult != null

                decisionReplyCasePriority != null
            }
        }
    }

    @Test
    'should receive review by enabled decision manager'()
    {
        given:
        def order = testCartForReview()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setDecisionManagerEnabled(true)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.ADVANCED_FRAUD_SCREEN
            transactionStatus == 'REVIEW'
            transactionStatusDetails == '480'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode == '480'
                decision == 'REVIEW'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                afsReplyReasonCode == '100'
                afsReplyAfsResult != null

                decisionReplyCasePriority != null
            }
        }
    }

    @Test
    'should not receive review if decision manager is disabled'()
    {
        given:
        def order = testCartForReview()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setDecisionManagerEnabled(false)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.ADVANCED_FRAUD_SCREEN
            transactionStatus != 'REVIEW'
            transactionStatusDetails != '480'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid
                reasonCode != '480'
                decision != 'REVIEW'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                afsReplyReasonCode == '100'
                afsReplyAfsResult != null
            }
        }
    }
}
