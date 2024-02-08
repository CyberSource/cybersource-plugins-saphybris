package isv.sap.payment.integration.common

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.verification.DeliveryAddressVerificationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class DeliveryAddressVerificationSpec extends IsvIntegrationSpec
{

    def builder = new DeliveryAddressVerificationRequestBuilder()

    @Test
    'Should receive ACCEPT for valid address'()
    {

        given:
        def address = "${getAddressMethod}"()
        def operationStartTime = new Date()
        def orderNumber = '123443219867'

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setMerchantReferenceCode(orderNumber)
                .addParam('address', address)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.DELIVERY_ADDRESS_VERIFICATION
            transactionStatusDetails == '100'
            transactionStatus == 'ACCEPT'

            requestId != null
            requestToken != null
            amount == null
            currency == null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                decision == 'ACCEPT'
                invalidField == '[]'
                missingField == '[]'
                merchantReferenceCode == orderNumber
                requestID != null
                requestToken != null
                reasonCode == '100'
                davReplyReasonCode == '100'
                davReplyIntlInfo == intlReply
                davReplyUsInfo == usReply
                davReplyCaInfo == caReply
                davReplyStandardizedCountry == country
                davReplyStandardizedPostalCode == zipCode
                davReplyStandardizedAddress1 == street
                davReplyStandardizedCity == city
                davReplyStandardizedISOCountry == isoCountry
                davReplyStandardizedState == state
                davReplyAddressType == addressType
                davReplyMatchScore != null
            }
        }

        where:
        getAddressMethod | intlReply | usReply  | caReply | country          | zipCode      | street                     | city            | isoCountry | state | addressType
        'getUKAddress'   | 'S4000'   | null     | null    | 'United Kingdom' | 'SW10 0UJ'   | '40 Gunter Gr'             | 'London'        | 'GB'       | 'LND' | 'S'
        'getUSAddress'   | null      | 'SA0000' | null    | 'US'             | '94108-5415' | '27 Maiden Ln'             | 'San Francisco' | 'US'       | 'CA'  | 'HD'
        'getDEAddress'   | 'SC000'   | null     | null    | 'Germany'        | '60313'      | 'Große Friedberger Str 14' | 'Frankfurt'     | 'DE'       | 'HE'  | 'S'
        'getCAAddress'   | null      | null     | 'S0180' | 'CA'             | 'M5B2G8'     | '65 Dundas St E'           | 'Toronto'       | 'CA'       | 'ON'  | 'F'
    }

    @Test
    'Should receive REJECT for invalid address'()
    {
        given:
        def address = "${getAddressMethod}"('INVALID')
        def operationStartTime = new Date()
        def orderNumber = '123443219867'

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setMerchantReferenceCode(orderNumber)
                .addParam('address', address)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.DELIVERY_ADDRESS_VERIFICATION
            transactionStatusDetails == '456'
            transactionStatus == 'REJECT'

            requestId != null
            requestToken != null
            amount == null
            currency == null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                decision == 'REJECT'
                invalidField == '[]'
                missingField == '[]'
                merchantReferenceCode == orderNumber
                requestID != null
                requestToken != null
                reasonCode == '456'
                davReplyReasonCode == '456'
                davReplyIntlErrorInfo == intlReply
                davReplyUsErrorInfo == usReply
                davReplyCaErrorInfo == caReply
            }
        }

        where:
        getAddressMethod | intlReply | usReply
        'getUKAddress'   | '3010'    | null
        'getUSAddress'   | null      | 'E412'
        'getDEAddress'   | '3010'    | null
    }

    @Test
    'Should receive REJECT for address not found'()
    {
        given:
        def address = "${getAddressMethod}"('MISSING_FIELDS')
        def operationStartTime = new Date()
        def orderNumber = '123443219867'

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setMerchantReferenceCode(orderNumber)
                .addParam('address', address)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.DELIVERY_ADDRESS_VERIFICATION
            transactionStatusDetails == responseCode
            transactionStatus == 'REJECT'

            requestId != null
            requestToken != null
            amount == null
            currency == null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                decision == 'REJECT'
                invalidField == '[]'
                missingField == '[]'
                merchantReferenceCode == orderNumber
                requestID != null
                requestToken != null
                reasonCode == responseCode
                davReplyReasonCode == responseCode
            }
        }

        where:
        getAddressMethod | _ | responseCode
        'getUKAddress'   | _ | '460'
        'getUSAddress'   | _ | '450'
        'getDEAddress'   | _ | '460'
    }
}
