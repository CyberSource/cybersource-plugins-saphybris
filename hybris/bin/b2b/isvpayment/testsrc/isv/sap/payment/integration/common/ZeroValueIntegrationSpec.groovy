package isv.sap.payment.integration.common

import de.hybris.bootstrap.annotations.ManualTest
import org.junit.Test
import spock.lang.PendingFeature

import isv.cjl.payment.service.executor.request.builder.GenericPaymentServiceRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.cjl.payment.enums.PaymentTransactionType.CAPTURE
import static isv.cjl.payment.enums.PaymentTransactionType.REFUND
import static isv.cjl.payment.enums.PaymentType.GIFT

@ManualTest
class ZeroValueIntegrationSpec extends IsvIntegrationSpec
{

    @Test
    @PendingFeature
    'should receive accept'()
    {
        given:
        def order = testOrderUk()
        def operationStartTime = new Date()

        when:
        def request = new GenericPaymentServiceRequestBuilder(GIFT, transactionType)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .build()
        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            type == transactionType
            decision == 'ACCEPT'
            purchaseTotals == order.totalPrice
            purchaseTotalsCurrency == order.currency.isocode

            time > operationStartTime
            time <= new Date()
        }
        where:
        transactionType << [AUTHORIZATION, CAPTURE, REFUND]
    }
}
