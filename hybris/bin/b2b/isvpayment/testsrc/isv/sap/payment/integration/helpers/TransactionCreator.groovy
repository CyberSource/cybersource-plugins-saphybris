package isv.sap.payment.integration.helpers

import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.sap.payment.service.PaymentTransactionService

/**
 * Created by dsurchis on 22/3/17.
 */
class TransactionCreator
{
    PaymentServiceExecutor paymentServiceExecutor
    TestConfig testConfig
    PaymentTransactionService paymentTransactionService

    protected static orderWithTransaction(order, transactionEntry)
    {
        if (order.paymentTransactions.isEmpty())
        {
            def paymentTransaction = transactionEntry.paymentTransaction
            paymentTransaction.entries = []
            order.paymentTransactions << paymentTransaction
        }

        order.paymentTransactions.first().entries << transactionEntry
        order
    }
}
