package isv.sap.payment.integration.helpers

import de.hybris.platform.payment.enums.PaymentTransactionType

import isv.cjl.payment.service.executor.request.builder.visacheckout.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.visacheckout.CaptureRequestBuilder
import isv.sap.payment.constants.IsvPaymentConstants

class VisaCheckoutTransactionsCreator extends TransactionCreator
{
    //set this value to true for e2e debugging
    private final vcDebug = false

    def addAuthorization(order, vcOrderId)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setVcOrderId(vcOrderId)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        if (!vcDebug)
        {
            transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()
            transactionEntry.currency = order.currency
            transactionEntry.amount = order.totalPrice
        }

        orderWithTransaction(order, transactionEntry)
    }

    def addCapture(order, vcOrderId)
    {
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()
        def request = new CaptureRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .setVcOrderId(vcOrderId)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        if (!vcDebug)
        {
            transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()
            transactionEntry.currency = order.currency
            transactionEntry.amount = order.totalPrice
//            transactionEntry.properties.put('payPalAuthorizationReplyTransactionId', '123')
        }

        orderWithTransaction(order, transactionEntry)
    }
}
