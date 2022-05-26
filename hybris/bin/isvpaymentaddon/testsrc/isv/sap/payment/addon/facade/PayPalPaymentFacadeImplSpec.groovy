package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.site.BaseSiteService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.exception.PaymentException
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.addon.facade.impl.PayPalPaymentFacadeImpl
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.cjl.payment.enums.PaymentTransactionType.CHECK_STATUS
import static isv.cjl.payment.enums.PaymentTransactionType.ORDER_SETUP
import static isv.cjl.payment.enums.PaymentType.PAY_PAL

@UnitTest
class PayPalPaymentFacadeImplSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    PaymentTransactionService paymentTransactionService = Mock()
    MerchantService merchantService = Mock()

    BaseSiteService baseSiteService = Mock()
    CartService cartService = Mock()
    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock()
    SiteConfigService siteConfigService = Mock()

    def facade = new PayPalPaymentFacadeImpl()

    PaymentTransactionModel transaction = Mock()

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()

    BaseSiteModel site = Mock()

    def paymentServiceResult = new PaymentServiceResult()

    def setup()
    {
        facade.merchantService = merchantService
        facade.paymentServiceExecutor = paymentServiceExecutor
        facade.paymentTransactionService = paymentTransactionService
        facade.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        facade.siteConfigService = siteConfigService
        facade.baseSiteService = baseSiteService

        cart.guid >> 'g-u-i-d'
        cart.paymentTransactions >> []
        merchantService.getCurrentMerchant(_) >> merchantModel
        merchantModel.id >> 'test_merchant'
        baseSiteService.currentBaseSite >> site

        cartService.sessionCart >> cart
        paymentServiceResult.addData(TRANSACTION, transactionEntry)
        transactionEntry.properties >> [
                'apSessionsReplyMerchantURL': 'EC-000111'
        ]
    }

    @Test
    def 'Do execute the paypal create session payment request'()
    {
        given:
        transactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ACCEPT
        facade.paypalRelativeCancelUrl = '/cancelURL'
        facade.paypalRelativeReturnUrl = '/returnURL'

        when:
        def result = facade.executePayPalExpressCheckoutCreateSessionRequest(cart)

        then:
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/cancelURL') >> 'https://localhost:9001/cancelURL'
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/returnURL') >> 'https://localhost:9001/returnURL'
        1 * paymentServiceExecutor.execute(_ as PaymentServiceRequest) >> paymentServiceResult
        result == 'EC-000111'
    }

    @Test
    def 'Should throw exception if paypal create session payment request is not successful'()
    {
        given:
        transactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ERROR
        facade.paypalRelativeCancelUrl = '/cancelURL'
        facade.paypalRelativeReturnUrl = '/returnURL'

        when:
        facade.executePayPalExpressCheckoutCreateSessionRequest(cart)

        then:
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/cancelURL') >> 'https://localhost:9001/cancelURL'
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/returnURL') >> 'https://localhost:9001/returnURL'
        1 * paymentServiceExecutor.execute(_ as PaymentServiceRequest) >> paymentServiceResult

        and:
        def exception = thrown(PaymentException)
        exception.message == "paypal Set action rejected with status [${transactionEntry.transactionStatus}]"
    }

    @Test
    def 'Fail when paypal set request has error status'()
    {
        given:
        transactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ERROR

        when:
        facade.executePayPalExpressCheckoutCreateSessionRequest(cart)

        then:
        thrown RuntimeException
    }

    @Test
    def 'Should authorize if all transactions were accepted'()
    {
        given:
        IsvPaymentTransactionModel payPalTxn = new IsvPaymentTransactionModel()
        payPalTxn.merchantId = 'test-isv'
        payPalTxn.paymentProvider = PaymentType.PAY_PAL.name()

        IsvPaymentTransactionEntryModel createSessionTxnEntry = new IsvPaymentTransactionEntryModel()
        createSessionTxnEntry.type = PaymentTransactionType.CREATE_SESSION
        createSessionTxnEntry.transactionStatus = PaymentConstants.TransactionStatus.ACCEPT
        createSessionTxnEntry.properties = ['apSessionsReplyMerchantURL': 'xxx']
        payPalTxn.entries = [createSessionTxnEntry]

        IsvPaymentTransactionEntryModel txnCheckStatus = new IsvPaymentTransactionEntryModel()
        txnCheckStatus.properties = ['apReplyPayerID': 'something']
        setUpTxnEntry(txnCheckStatus, payPalTxn)
        IsvPaymentTransactionEntryModel txnOrderSetup = new IsvPaymentTransactionEntryModel()
        setUpTxnEntry(txnOrderSetup, payPalTxn)
        IsvPaymentTransactionEntryModel txnAuth = new IsvPaymentTransactionEntryModel()
        setUpTxnEntry(txnAuth, payPalTxn)

        paymentTransactionService.getLatestAcceptedTransactionEntry(payPalTxn, PaymentTransactionType.CREATE_SESSION) >> Optional.of(createSessionTxnEntry)

        when:
        facade.authorizePayPalPayment(cart, 'xxx')

        then:
        cart.paymentTransactions >> [payPalTxn]
        1 * paymentServiceExecutor.execute {
            checkRequest(it, CHECK_STATUS, cart, createSessionTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnCheckStatus)

        1 * paymentServiceExecutor.execute {
            checkRequest(it, ORDER_SETUP, cart, createSessionTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnOrderSetup)

        1 * paymentServiceExecutor.execute {
            checkRequest(it, AUTHORIZATION, cart, txnOrderSetup)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnAuth)
    }

    @Test
    def 'Should return false if check status transaction is not accepted'()
    {
        given:
        IsvPaymentTransactionModel payPalTxn = new IsvPaymentTransactionModel()
        payPalTxn.merchantId = 'test-isv'
        payPalTxn.paymentProvider = PaymentType.PAY_PAL.name()

        IsvPaymentTransactionEntryModel createSessionTxnEntry = new IsvPaymentTransactionEntryModel()
        createSessionTxnEntry.type = PaymentTransactionType.CREATE_SESSION
        createSessionTxnEntry.transactionStatus = PaymentConstants.TransactionStatus.ACCEPT
        createSessionTxnEntry.properties = ['apSessionsReplyMerchantURL': 'xxx']
        payPalTxn.entries = [createSessionTxnEntry]

        IsvPaymentTransactionEntryModel txnCheckStatus = new IsvPaymentTransactionEntryModel()
        txnCheckStatus.transactionStatus = PaymentConstants.TransactionStatus.ERROR

        paymentTransactionService.getLatestAcceptedTransactionEntry(payPalTxn, PaymentTransactionType.CREATE_SESSION) >> Optional.of(createSessionTxnEntry)

        when:
        def result = facade.authorizePayPalPayment(cart, 'xxx')

        then:
        cart.paymentTransactions >> [payPalTxn]
        1 * paymentServiceExecutor.execute {
            checkRequest(it, CHECK_STATUS, cart, createSessionTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnCheckStatus)

        and:
        !result
    }

    @Test
    def 'Should return false if order setup transaction is not accepted'()
    {
        given:
        IsvPaymentTransactionModel payPalTxn = new IsvPaymentTransactionModel()
        payPalTxn.merchantId = 'test-isv'
        payPalTxn.paymentProvider = PaymentType.PAY_PAL.name()

        IsvPaymentTransactionEntryModel createSessionTxnEntry = new IsvPaymentTransactionEntryModel()
        createSessionTxnEntry.type = PaymentTransactionType.CREATE_SESSION
        createSessionTxnEntry.transactionStatus = PaymentConstants.TransactionStatus.ACCEPT
        createSessionTxnEntry.properties = ['apSessionsReplyMerchantURL': 'xxx']
        payPalTxn.entries = [createSessionTxnEntry]

        IsvPaymentTransactionEntryModel txnCheckStatus = new IsvPaymentTransactionEntryModel()
        txnCheckStatus.properties = ['apReplyPayerID': 'something']
        setUpTxnEntry(txnCheckStatus, payPalTxn)
        IsvPaymentTransactionEntryModel txnOrderSetup = new IsvPaymentTransactionEntryModel()
        txnOrderSetup.transactionStatus = PaymentConstants.TransactionStatus.ERROR

        paymentTransactionService.getLatestAcceptedTransactionEntry(payPalTxn, PaymentTransactionType.CREATE_SESSION) >> Optional.of(createSessionTxnEntry)

        when:
        def result = facade.authorizePayPalPayment(cart, 'xxx')

        then:
        cart.paymentTransactions >> [payPalTxn]
        1 * paymentServiceExecutor.execute {
            checkRequest(it, CHECK_STATUS, cart, createSessionTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnCheckStatus)

        1 * paymentServiceExecutor.execute {
            checkRequest(it, ORDER_SETUP, cart, createSessionTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnOrderSetup)

        and:
        !result
    }

    @Test
    def 'Should authorize if auth transaction is in review'()
    {
        given:
        IsvPaymentTransactionModel payPalTxn = new IsvPaymentTransactionModel()
        payPalTxn.merchantId = 'test-isv'
        payPalTxn.paymentProvider = PaymentType.PAY_PAL.name()
        IsvPaymentTransactionEntryModel payPalTxnEntry = new IsvPaymentTransactionEntryModel()
        payPalTxnEntry.type = PaymentTransactionType.CREATE_SESSION
        payPalTxnEntry.transactionStatus = PaymentConstants.TransactionStatus.ACCEPT
        payPalTxnEntry.properties = ['apSessionsReplyMerchantURL': 'xxx']
        payPalTxn.entries = [payPalTxnEntry]

        IsvPaymentTransactionEntryModel txnCheckStatus = new IsvPaymentTransactionEntryModel()
        txnCheckStatus.properties = ['apReplyPayerID': 'something']
        setUpTxnEntry(txnCheckStatus, payPalTxn)
        IsvPaymentTransactionEntryModel txnOrderSetup = new IsvPaymentTransactionEntryModel()
        setUpTxnEntry(txnOrderSetup, payPalTxn)
        IsvPaymentTransactionEntryModel txnAuth = new IsvPaymentTransactionEntryModel()
        setUpTxnEntry(txnAuth, PaymentConstants.TransactionStatus.REVIEW, payPalTxn)

        paymentTransactionService.getLatestAcceptedTransactionEntry(payPalTxn, PaymentTransactionType.CREATE_SESSION) >> Optional.of(payPalTxnEntry)

        when:
        facade.authorizePayPalPayment(cart, 'xxx')

        then:
        cart.paymentTransactions >> [payPalTxn]
        1 * paymentServiceExecutor.execute {
            checkRequest(it, CHECK_STATUS, cart, payPalTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnCheckStatus)

        1 * paymentServiceExecutor.execute {
            checkRequest(it, ORDER_SETUP, cart, payPalTxnEntry)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnOrderSetup)

        1 * paymentServiceExecutor.execute {
            checkRequest(it, AUTHORIZATION, cart, txnOrderSetup)
        } >> PaymentServiceResult.create().addData(TRANSACTION, txnAuth)
    }

    @Test
    def 'Should not authorize and throw ex if no matching SET transaction exists'()
    {
        when:
        facade.authorizePayPalPayment(cart, 'xxx')

        then:
        IllegalStateException ex = thrown()
        ex.message == "Cart: ${cart.guid} doesn't have valid paypal CREATE SESSION transaction entry"
    }

    def checkRequest(PaymentServiceRequest request, txnType, cart, txn)
    {
        request.paymentType == PAY_PAL &&
                request.paymentTransactionType == txnType &&
                request.getParam(ORDER).is(cart) &&
                request.getParam(TRANSACTION).is(txn)
    }

    private void setUpTxnEntry(entry, txn)
    {
        entry.setTransactionStatus(PaymentConstants.TransactionStatus.ACCEPT)
        entry.setPaymentTransaction(txn)
    }

    private void setUpTxnEntry(entry, status, txn)
    {
        entry.setTransactionStatus(status)
        entry.setPaymentTransaction(txn)
    }
}
