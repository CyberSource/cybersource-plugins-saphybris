package isv.sap.payment.integration.helpers

import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.dto.CardInfo
import de.hybris.platform.payment.enums.PaymentTransactionType

import isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.creditcard.CaptureRequestBuilder
import isv.cjl.payment.service.executor.request.builder.creditcard.PaymentTokenCreateRequestBuilder
import isv.cjl.payment.service.executor.request.builder.creditcard.RefundStandaloneRequestBuilder

class CreditCardTransactionsCreator extends TransactionCreator
{

    def addAuthorization(AbstractOrderModel order, CardInfo cardInfo)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', cardInfo)
                .build()
        def result = paymentServiceExecutor.execute(request)

        def transactionEntry = result.getData('transaction')

        orderWithTransaction(order, transactionEntry)
    }

    def addPaymentToken(AbstractOrderModel order)
    {
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()
        def request = new PaymentTokenCreateRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        orderWithTransaction(order, transactionEntry)
    }

    def addCapture(AbstractOrderModel order)
    {
        def authorization = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()
        def request = new CaptureRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        orderWithTransaction(order, transactionEntry)
    }

    def addRefundStandalone(AbstractOrderModel order, CardInfo card)
    {
        def request = new RefundStandaloneRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('card', card)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        orderWithTransaction(order, transactionEntry)
    }

    def getSubscription(AbstractOrderModel order)
    {
        def transactionEntry = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), PaymentTransactionType.AUTHORIZATION).get()
        def request = new PaymentTokenCreateRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', transactionEntry)
                .build()

        def result = paymentServiceExecutor.execute(request)
        result.getData('transaction').subscriptionID
    }
}
