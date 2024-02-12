package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.sap.payment.addon.facade.impl.GooglePayPaymentFacadeImpl
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.paymentData

@UnitTest
class GooglePayPaymentFacadeImplSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    MerchantService merchantService = Mock()

    def facade = new GooglePayPaymentFacadeImpl()

    PaymentTransactionModel transaction = Mock()

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()

    def setup()
    {
        facade.merchantService = merchantService
        facade.paymentServiceExecutor = paymentServiceExecutor

        cart.paymentTransactions >> []
        merchantService.getCurrentMerchant(_) >> merchantModel
        merchantModel.id >> 'test_merchant'
    }

    @Test
    def 'Should authorize Google Pay payment'()
    {
        given:
        transactionEntry.transactionStatus >> transactionStatus

        when:
        def result = facade.authorizeGooglePayPayment(paymentData, cart)

        then:
        1 * paymentServiceExecutor.execute(_) >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)
        result == expectedResult

        where:
        transactionStatus                                    | expectedResult
        PaymentConstants.TransactionStatus.ACCEPT | true
        PaymentConstants.TransactionStatus.REJECT | false
    }
}
