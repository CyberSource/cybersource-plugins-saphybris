package isv.sap.payment.integration.helpers

import com.google.common.base.Splitter
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.builder.alternative.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.AuthorizationReversalRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.CreateSessionRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.InitiateRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.OptionsRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.RefundRequestBuilder
import isv.cjl.payment.service.executor.request.builder.alternative.SaleRequestBuilder
import isv.sap.payment.constants.IsvPaymentConstants

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

class AlternativePaymentTransactionsCreator extends TransactionCreator
{
    def addInitiate(AbstractOrderModel order, AlternativePaymentMethod paymentMethod)
    {
        def request = new InitiateRequestBuilder()
                .setApPaymentType(paymentMethod)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setProductName(order.code)
                .setReturnUrl(testConfig.returnUrl)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def addSale(AbstractOrderModel order, AlternativePaymentMethod paymentMethod, String optionId = null)
    {
        def request = new SaleRequestBuilder()
                .setApPaymentType(paymentMethod)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setMerchantDescriptor('descriptor')
                .setCancelURL(testConfig.cancelUrl)
                .setSuccessURL(testConfig.returnUrl)
                .setFailureURL(testConfig.failUrl)
                .setPaymentOptionID(optionId ?: testConfig.idealOption)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        println transactionEntry.properties.merchantURL

        orderWithTransaction(order, transactionEntry)
    }

    def addCheckStatus(AbstractOrderModel order,
                       PaymentTransactionType transactionType,
                       AlternativePaymentMethod paymentMethod,
                       String reconciliationId = null)
    {
        def paymentTransaction = paymentTransactionService.getLatestAcceptedTransactionEntry(
                order.paymentTransactions.first(), transactionType).get()

        def request = new CheckStatusRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setReconciliationId(reconciliationId)
                .addParam('order', order)
                .addParam('transaction', paymentTransaction)
                .addParam('alternatePaymentMethod', paymentMethod)
                .addParam('paymentTransactionType', isv.cjl.payment.enums.PaymentTransactionType.valueOf(transactionType.code))
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def createKlarnaSession(AbstractOrderModel order)
    {
        def request = new CreateSessionRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        orderWithTransaction(order, transactionEntry)
    }

    def addRefund(AbstractOrderModel order, AlternativePaymentMethod paymentMethod)
    {
        def request = new RefundRequestBuilder()
                .setApPaymentType(paymentMethod)
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .addParam('alternatePaymentMethod', paymentMethod)
                .setAmount(order.totalPrice)
                .setReason('reason')
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData('transaction')

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()

        orderWithTransaction(order, transactionEntry)
    }

    def addAuthorization(AbstractOrderModel order, String preAuthToken, String language)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setPreapprovalToken(preAuthToken)
                .setCancelURL(testConfig.klarnaCancelUrl)
                .setFailureURL(testConfig.klarnaFailureUrl)
                .setSuccessURL(testConfig.klarnaSuccessUrl)
                .setBillToLanguage(language)
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()
        transactionEntry.currency = order.currency
        transactionEntry.amount = order.totalPrice

        orderWithTransaction(order, transactionEntry)
    }

    def addAuthorizationReversal(AbstractOrderModel order)
    {
        def request = new AuthorizationReversalRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()
        transactionEntry.currency = order.currency
        transactionEntry.amount = order.totalPrice

        orderWithTransaction(order, transactionEntry)
    }

    def addRefund(AbstractOrderModel order)
    {
        def request = new RefundRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .setAmount(order.totalPrice)
                .setApPaymentType(AlternativePaymentMethod.KLI)
                .addParam('transaction', order.paymentTransactions.first().entries.first())
                .build()

        def result = paymentServiceExecutor.execute(request)
        def transactionEntry = result.getData(TRANSACTION)

        transactionEntry.transactionStatus = IsvPaymentConstants.TransactionStatus.ACCEPT.toString()
        transactionEntry.transactionStatusDetails = IsvPaymentConstants.ReasonCode.OK
        transactionEntry.currency = order.currency
        transactionEntry.amount = order.totalPrice

        orderWithTransaction(order, transactionEntry)
    }

    def getFirstOptionId()
    {
        def optionsRequestBuilder = new OptionsRequestBuilder()
        def request = optionsRequestBuilder.setMerchantId(testConfig.merchant).build()

        def result = paymentServiceExecutor.execute(request)

        def banksMapString = result.getData(TRANSACTION).properties['options'] as String
        def banksMapClean = banksMapString.replaceAll('[{}]', '')
        def banksMap = Splitter.on(',').trimResults().omitEmptyStrings().withKeyValueSeparator('=').split(banksMapClean)
        banksMap.entrySet().first().key
    }
}
