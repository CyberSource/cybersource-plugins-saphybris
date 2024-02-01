package isv.sap.payment.integration.onlinebanking

import de.hybris.bootstrap.annotations.ManualTest
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.alternative.OptionsRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

@ManualTest
class OptionsIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new OptionsRequestBuilder()

    @Test
    def 'Should receive list of ideal banks'()
    {
        given:
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            type == de.hybris.platform.payment.enums.PaymentTransactionType.OPTIONS
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

                apOptionsReplyOffset != null

                //lets count number of banks, rather than keep changing this validation
                //now the number is 11 banks
                // {ideal-BUNQNL2A=bunq, ideal-RBRBNL21=RegioBank, ideal-ASNBNL21=ASN Bank, ideal-FVLBNL22=van Lanschot, ideal-TRIONL2U=Triodos Bank, ideal-MOYONL21=Moneyou, ideal-KNABNL2H=Knab, ideal-INGBNL2A=ING, ideal-SNSBNL2A=SNS Bank, ideal-ABNANL2A=ABN AMRO, ideal-RABONL2U=Rabobank}
                (options as String).split(',').size().toString() == apOptionsReplyTotalCount
                // lets assume we get back at least one bank
                apOptionsReplyTotalCount > '1'
                apOptionsReplyCount == apOptionsReplyTotalCount
                apOptionsReplyResponseCode == '00000'
            }
        }
    }
}
