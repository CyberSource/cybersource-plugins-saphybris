package isv.sap.payment.integration.creditcard

import com.google.gson.Gson
import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.TaxRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class TaxIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new TaxRequestBuilder()

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
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.TAX
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            code.toString().contains(order.code)
            requestId != null
            requestToken != null

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

                taxReplyGrandTotalAmount != null
                taxReplyTotalCountryTaxAmount != null
                taxReplyTotalCountyTaxAmount != null
                taxReplyTotalCityTaxAmount != null
                taxReplyTotalStateTaxAmount != null
                taxReplyTotalTaxAmount != null
                taxReplyItems != null
            }
        }
        and:
        def gson = new Gson()
        String taxItemsJson = result.getData('transaction').properties.taxReplyItems
        def taxItems = gson.fromJson(taxItemsJson, Collection)
        if (taxItems.size() > 0)
        {
            with(taxItems.first()) {
                id == 0
                totalTaxAmount != null
            }
        }
    }

    @Test
    'should receive reject for missing data'()
    {
        given:
        def order = testOrderMissingFields()
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == PaymentTransactionType.TAX
            transactionStatus == 'REJECT'
            transactionStatusDetails == '101'

            code.toString().contains(order.code)
            requestId != null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                reasonCode == '101'
                decision == 'REJECT'

                invalidField == '[]'
                missingField == '[c:shipTo/c:state]'
            }
        }
    }
}
