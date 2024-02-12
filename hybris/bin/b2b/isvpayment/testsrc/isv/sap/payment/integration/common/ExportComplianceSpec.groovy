package isv.sap.payment.integration.common

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import groovy.json.JsonSlurper
import org.junit.Test

import isv.cjl.payment.enums.ExportComplianceAddressOperator
import isv.cjl.payment.enums.ExportComplianceFieldWeight
import isv.cjl.payment.service.executor.request.builder.verification.ExportComplianceRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class ExportComplianceSpec extends IsvIntegrationSpec
{

    def builder = new ExportComplianceRequestBuilder()

    @Test
    'Should receive ACCEPT for valid address US'()
    {

        given:
        def order = testOrderUs()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.EXPORT_COMPLIANCE
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
                merchantReferenceCode == order.guid
                requestID != null
                requestToken != null
                reasonCode == '100'
                exportReplyReasonCode == '100'
                deniedPartiesMatch == '[]'
                exportReplyIpCountryConfidence == '-1'
            }
        }
    }

    @Test
    'Should receive ACCEPT for valid address UK'()
    {
        given:
        def order = testOrderUk()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.EXPORT_COMPLIANCE
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
                merchantReferenceCode == order.guid
                requestID != null
                requestToken != null
                reasonCode == '100'
                exportReplyReasonCode == '100'
                deniedPartiesMatch == '[]'
                exportReplyIpCountryConfidence == '-1'
            }
        }
    }

    @Test
    'Should create transaction for denied parties list'()
    {
        given:
        def order = testOrderNonCompliantName()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.EXPORT_COMPLIANCE
            transactionStatusDetails == '700'
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
                merchantReferenceCode == order.guid
                requestID != null
                requestToken != null
                reasonCode == '700'
                exportReplyReasonCode == '700'
                exportReplyInfoCode == 'MATCH-DPC'
                deniedPartiesMatch != null
                def jsonSlurper = new JsonSlurper()
                def deniedPartiesObj = jsonSlurper.parseText(deniedPartiesMatch)
                deniedPartiesObj.name[0][0] == 'Mohammed QASIM'
                deniedPartiesObj.name[0][1] == 'aka, QASIM, Muhammad'
                deniedPartiesObj.address[0][0] == ',Waish,Spin Boldak,null,'
                deniedPartiesObj.address[0][1] == 'Bypass Road,Chaman,,null,'
                deniedPartiesObj.address[0][2] == "Karez Qaran,Musa Qal'ah,Helmand Province,null,"
                deniedPartiesObj.address[0][3] == 'Qalaye Haji Ali Akbar Dalbandin Post Office,,,null,'
                deniedPartiesObj.address[0][4] == 'Room 33, 5th Floor Sarafi Market,Kandahar,,null,'
                deniedPartiesObj.address[0][5] == 'Safaar Bazaar,Garmsir,,null,'
                deniedPartiesObj.list[0] == 'Office of Foreign Assets Control'
                deniedPartiesObj.program[0][0] == 'SDGT'
            }
        }
    }

    @Test
    'Should receive REJECT for non compliant address'()
    {
        given:
        def order = testOrderNonCompliantAddress()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setAddressOperator(ExportComplianceAddressOperator.OR)
                .setAddressWeight(ExportComplianceFieldWeight.LOW)
                .setNameWeight(ExportComplianceFieldWeight.LOW)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.EXPORT_COMPLIANCE
            transactionStatusDetails == '700'
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
                merchantReferenceCode == order.guid
                requestID != null
                requestToken != null
                reasonCode == '700'
                exportReplyReasonCode == '700'
                deniedPartiesMatch != null
                def jsonSlurper = new JsonSlurper()
                def deniedPartiesObj = jsonSlurper.parseText(deniedPartiesMatch)
                deniedPartiesObj.list[0] == 'Office of Foreign Assets Control'
                deniedPartiesObj.name[0][0] == 'Maria de Jesus ESPINOZA RODRIGUEZ'
                deniedPartiesObj.program[0][0] == 'SDNTK'
            }
        }
    }

    @Test
    'Should receive ACCEPT for valid address DE'()
    {
        given:
        def order = testOrderDe()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setAddressOperator(ExportComplianceAddressOperator.AND)
                .setAddressWeight(ExportComplianceFieldWeight.HIGH)
                .setNameWeight(ExportComplianceFieldWeight.MEDIUM)
                .setCompanyWeight(ExportComplianceFieldWeight.EXACT)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.EXPORT_COMPLIANCE
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
                merchantReferenceCode == order.guid
                requestID != null
                requestToken != null
                reasonCode == '100'
                exportReplyReasonCode == '100'
                deniedPartiesMatch == '[]'
                exportReplyIpCountryConfidence == '-1'
            }
        }
    }
}
