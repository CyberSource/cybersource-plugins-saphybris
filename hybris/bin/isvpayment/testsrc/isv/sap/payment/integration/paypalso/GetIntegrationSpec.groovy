package isv.sap.payment.integration.paypalso

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypalso.GetRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION

@ManualTest
class GetIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new GetRequestBuilder()

    @Test
    'should receive accept'()
    {
        given:
        def order = testOrderUk()

        def setTransaction = ppSoTransactionCreator.createSetTransaction(order)
        def payPalToken = setTransaction.properties.payPalEcSetReplyPaypalToken

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(SET_TRANSACTION, setTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL_SO.name()
            type == PaymentTransactionType.GET
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                payPalEcGetDetailsReplyPaypalToken == payPalToken
                merchantReferenceCode == order.guid
                reasonCode == '100'
                decision == 'ACCEPT'

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                payPalEcGetDetailsReplyCorrelationID != null
                payPalEcGetDetailsReplyReasonCode == '100'
            }
        }
    }
}
