package isv.sap.payment.integration.klarna

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

/**
 * Created by esanchez on 9/6/17.
 */
@ManualTest
class CheckStatusIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new CheckStatusRequestBuilder()

    @Test
    'Should not accept Klarna Check Status after authorization for non-existing preauthorization token'()
    {
        given:
        def order = testOrderUk()

        def preApprovalToken = testKlarnaPreauthorizationToken()
        apTransactionCreator.addInitiate(order, AlternativePaymentMethod.KLI)
        apTransactionCreator.addAuthorization(order, preApprovalToken, locale)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setPaymentTransactionType(isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', AlternativePaymentMethod.KLI)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.CHECK_STATUS
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            requestToken != null
            subscriptionID == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                apCheckStatusReplyReasonCode == '102'
                reasonCode == '102'
                decision == 'REJECT'
            }
        }

        where:
        locale  | _
        'en-GB' | _
        'de-DE' | _
    }
}
