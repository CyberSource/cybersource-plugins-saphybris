package isv.sap.payment.integration.helpers

import isv.cjl.payment.service.executor.request.builder.paypalso.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypalso.CaptureRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypalso.GetRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypalso.OrderSetupRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypalso.SetRequestBuilder

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.GET_TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER_SETUP_TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

class PayPalSoTransactionsCreator extends TransactionCreator
{
    def createSetTransaction(order)
    {
        def request = new SetRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setReturnURL(testConfig.returnUrl)
                .setCancelURL(testConfig.cancelUrl)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)

        result.getData(TRANSACTION)
    }

    def createGetTransaction(order, setTransaction)
    {
        def request = new GetRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam(SET_TRANSACTION, setTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transaction = result.getData(TRANSACTION)
        transaction.properties.payPalEcGetDetailsReplyPayerId = 'fakeId'
        transaction.properties.payPalEcGetDetailsReplyPayer = 'fakeEmail@fake.co'
        transaction
    }

    def createOrderSetup(order, setTransaction, getTransaction)
    {

        def request = new OrderSetupRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(SET_TRANSACTION, setTransaction)
                .addParam(GET_TRANSACTION, getTransaction)
                .build()
        def result = paymentServiceExecutor.execute(request)

        def transaction = result.getData(TRANSACTION)
        transaction.properties.payPalEcOrderSetupReplyTransactionId = '123'
        transaction
    }

    def createAuthorization(order, getTransaction, orderSetupTransaction)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(GET_TRANSACTION, getTransaction)
                .addParam(ORDER_SETUP_TRANSACTION, orderSetupTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transaction = result.getData(TRANSACTION)
        transaction.properties.payPalAuthorizationReplyTransactionId = '123'
        transaction
    }

    def createCapture(order, authorizationTransaction)
    {
        def request = new CaptureRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam(ORDER, order)
                .addParam(TRANSACTION, authorizationTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transaction = result.getData(TRANSACTION)
        transaction.properties.payPalDoCaptureReplyTransactionId = '123'
        transaction
    }
}
