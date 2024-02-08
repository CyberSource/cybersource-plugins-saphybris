package isv.sap.payment.integration.paypalso

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypalso.AuthorizationRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.GET_TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER_SETUP_TRANSACTION

@ManualTest
class AuthorizationIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new AuthorizationRequestBuilder()

    @Test
    'should receive reject for invalid paypal order id'()
    {
        given:
        def order = testOrderUk()
        def setTransaction = ppSoTransactionCreator.createSetTransaction(order)
        def getTransaction = ppSoTransactionCreator.createGetTransaction(order, setTransaction)
        def orderSetupTransaction = ppSoTransactionCreator.createOrderSetup(order, setTransaction, getTransaction)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(GET_TRANSACTION, getTransaction)
                .addParam(ORDER_SETUP_TRANSACTION, orderSetupTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL_SO.name()
            type == PaymentTransactionType.AUTHORIZATION
            transactionStatus == 'REJECT'
            transactionStatusDetails == '233'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                merchantReferenceCode == order.guid

                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                payPalAuthorizationReplyReasonCode == '233'
                payPalAuthorizationReplyErrorCode == '10609'
            }
        }
    }
}
