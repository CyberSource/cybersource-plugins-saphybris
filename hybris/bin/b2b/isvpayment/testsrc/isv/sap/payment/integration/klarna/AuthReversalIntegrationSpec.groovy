package isv.sap.payment.integration.klarna

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.service.executor.request.builder.alternative.AuthorizationReversalRequestBuilder
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

/**
 * Created by esanchez on 9/6/17.
 */
@ManualTest
class AuthReversalIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new AuthorizationReversalRequestBuilder()

    @Test
    'Should not reverse Klarna authorization for non-existing preauthorization token'()
    {
        given:
        def order = testOrderUk()

        def preApprovalToken = testKlarnaPreauthorizationToken()
        apTransactionCreator.addAuthorization(order, preApprovalToken, locale)
        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData(TRANSACTION)) {
            paymentTransaction.paymentProvider == PaymentType.ALTERNATIVE_PAYMENT.name()
            type == PaymentTransactionType.AUTHORIZATION_REVERSAL
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

                apAuthReversalReplyReasonCode == '102'
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
