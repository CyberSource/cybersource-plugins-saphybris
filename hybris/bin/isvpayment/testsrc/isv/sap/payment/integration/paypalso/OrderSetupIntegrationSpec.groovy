package isv.sap.payment.integration.paypalso

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.builder.paypalso.OrderSetupRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.GET_TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION

@ManualTest
class OrderSetupIntegrationSpec extends IsvIntegrationSpec
{

    def builder = new OrderSetupRequestBuilder()

//    @PendingFeature
    @Test
    'should receive reject for not authorized payment request'()
    {
        given:
        def order = testCartUk()
        def setTransaction = ppSoTransactionCreator.createSetTransaction(order)
        def getTransaction = ppSoTransactionCreator.createGetTransaction(order, setTransaction)

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(SET_TRANSACTION, setTransaction)
                .addParam(GET_TRANSACTION, getTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == PaymentType.PAY_PAL_SO.name()
            type == PaymentTransactionType.ORDER_SETUP
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

                payPalEcOrderSetupReplyReasonCode == '233'
                payPalEcOrderSetupReplyErrorCode == '10406'
            }
        }
    }
}
