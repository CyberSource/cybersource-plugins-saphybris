package isv.sap.payment.integration.helpers

import de.hybris.platform.payment.enums.PaymentTransactionType

import isv.cjl.payment.service.executor.request.builder.paypal.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypal.CaptureRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypal.CheckStatusRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypal.CreateBillingAgreementSessionRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypal.CreateSessionRequestBuilder
import isv.cjl.payment.service.executor.request.builder.paypal.OrderSetupRequestBuilder
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

class PayPalApTransactionsCreator extends TransactionCreator
{
    static final FAKE_ID = 'FAKE'

    def addCreateSessionTransaction(order)
    {
        def request = new CreateSessionRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setSuccessURL(testConfig.returnUrl)
                .setCancelURL(testConfig.cancelUrl)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def addCreateBillingAgreementSessionTransaction(order)
    {
        def request = new CreateBillingAgreementSessionRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setSuccessURL(testConfig.returnUrl)
                .setCancelURL(testConfig.cancelUrl)
                .addParam('order', order)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def addCheckStatusTransaction(order)
    {
        def createSession = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SESSION).get()
        def request = new CheckStatusRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', createSession)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def addOrderSetup(order)
    {
        def sessionTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.CREATE_SESSION).get()
        def request = new OrderSetupRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('PayerID', FAKE_ID)
                .addParam('transaction', sessionTransaction)
                .build()

        def result = paymentServiceExecutor.execute(request)
        IsvPaymentTransactionEntryModel transactionEntry = result.getData(TRANSACTION)

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()

        orderWithTransaction(order, transactionEntry)
    }

    def addAuthorization(order)
    {
        def orderSetup = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.ORDER_SETUP).get()

        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', orderSetup)
                .build()

        def result = paymentServiceExecutor.execute(request)
        IsvPaymentTransactionEntryModel transactionEntry = result.getData(TRANSACTION)

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()

        orderWithTransaction(order, transactionEntry)
    }

    def addCapture(order)
    {
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()

        def request = new CaptureRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)
        IsvPaymentTransactionEntryModel transactionEntry = result.getData(TRANSACTION)
        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()

        orderWithTransaction(order, transactionEntry)
    }
}
