package isv.sap.payment.integration.reporting

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.util.concurrent.PollingConditions

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.PaymentSource
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
class ReportingAuthorizationIntegrationSpec extends IsvIntegrationSpec
{
    @Test
    def 'should create an additional transaction entry as a result of reporting authorization command'()
    {
        given:
        def order = testOrderUk()
        def cardInfo = testCard()
        def operationStartTime = new Date()
        def conditions = new PollingConditions()

        and: 'authorization transaction available'
        paymentServiceExecutor.execute(new AuthorizationRequestBuilder()
                                               .setMerchantId(testConfig.merchant)
                                               .addParam('order', order)
                                               .addParam('card', cardInfo)
                                               .build())

        when:
        def request = PaymentServiceRequest.create()
                .service(isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION)
                .source(PaymentSource.REPORTING)
                .method(PaymentType.CREDIT_CARD)
                .addParam(PaymentConstants.CommonFields.MERCHANT_ID, testConfig.merchant)
                .addParam(PaymentConstants.CommonFields.ORDER, order)

        then:
        conditions.within(60) {
            def result = paymentServiceExecutor.execute(request)

            with(result.getData('transaction')) {
                type == PaymentTransactionType.AUTHORIZATION
                transactionStatus == 'ACCEPT'
                transactionStatusDetails == '100'

                amount == order.totalPrice
                code.toString().contains(order.code)
                requestId != null
                currency.isocode == order.currency.isocode

                time > operationStartTime
                time <= new Date()

                with(properties) {
                    reasonCode == '100'
                    decision == 'ACCEPT'
                    requestID != null
                }
            }
        }
    }
}
