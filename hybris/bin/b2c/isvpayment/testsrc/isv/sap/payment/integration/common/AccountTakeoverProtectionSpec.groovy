package isv.sap.payment.integration.common

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import io.qala.datagen.RandomShortApi
import org.junit.Test

import isv.cjl.payment.enums.DmeServiceEventType
import isv.cjl.payment.service.executor.request.builder.fraud.AccountTakeoverProtectionRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class AccountTakeoverProtectionSpec extends IsvIntegrationSpec
{
    def builder = new AccountTakeoverProtectionRequestBuilder()

    // Please Note: this test depends on ISV settings of Device fingerprint validation
    // 2 different behaviors
    // -- ACCEPT with Reason Code 100, if validation is turned off
    // -- REJECT with Reason Code 102, if validation is turned on
    // Current behavior -- ACCEPT

    @Test
    def 'should receive reject for random device fingerprint'()
    {
        given:
        def fingerprintId = RandomShortApi.alphanumeric(36)
        def referenceId = RandomShortApi.alphanumeric(35)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('deviceFingerprintID', fingerprintId)
                .addParam('merchantReferenceCode', referenceId)
                .addParam('dmeServiceEventType', event.toString())
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.ACCOUNT_TAKEOVER_PROTECTION
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode != null
                reasonCode == '100'
                decision == 'ACCEPT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null
            }
        }

        where:
        event                                | _
        DmeServiceEventType.ACCOUNT_CREATION | _
        DmeServiceEventType.ACCOUNT_UPDATE   | _
        DmeServiceEventType.LOGIN            | _
    }
}
