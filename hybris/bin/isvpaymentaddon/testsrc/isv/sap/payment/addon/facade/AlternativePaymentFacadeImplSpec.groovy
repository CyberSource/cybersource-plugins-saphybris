package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.PaymentModeService
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.payment.model.PaymentTransactionModel
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.sap.payment.addon.facade.impl.AlternativePaymentFacadeImpl
import isv.sap.payment.addon.strategy.AlternativePaymentSaleRequester
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.model.IsvPaymentModeModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel

import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ERROR
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REJECT
import static isv.cjl.payment.enums.AlternativePaymentMethod.IDL
import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.PAYMENT_OPTION_ID
import static isv.sap.payment.enums.AlternativePaymentMethod.APY
import static isv.sap.payment.enums.AlternativePaymentMethod.AYM
import static isv.sap.payment.enums.AlternativePaymentMethod.KLI
import static isv.sap.payment.enums.AlternativePaymentMethod.SOF
import static isv.sap.payment.enums.AlternativePaymentMethod.WQR
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT

@UnitTest
class AlternativePaymentFacadeImplSpec extends Specification
{
    MerchantService merchantService = Mock()
    PaymentModeService paymentModeService = Mock()

    def facade = new AlternativePaymentFacadeImpl(merchantService: merchantService,
                                                  paymentModeService: paymentModeService)

    CartModel cart = Mock()

    Merchant merchant = Mock()
    PaymentTransactionModel transaction = Mock()

    def setup()
    {
        merchantService.getCurrentMerchant(_) >> merchant
        merchant.id >> 'test_merchant'
    }

    @Test
    def 'makeSaleRequestForAlternativePayment: Should return URL to third party payment gateway'()
    {
        given:
        AlternativePaymentSaleRequester saleRequester = Mock()
        facade.saleRequesters = [saleRequester]

        def cart = new CartModel()

        def pm = new IsvPaymentModeModel()
        pm.paymentType = ALTERNATIVE_PAYMENT
        pm.paymentSubType = AlternativePaymentMethod.IDL

        def result = PaymentServiceResult.create()
        def entry = new IsvPaymentTransactionEntryModel()
        result.addData('transaction', entry)
        entry.transactionStatus = ACCEPT
        entry.properties = [(PaymentConstants.AlternativePaymentsResponseFields.MERCHANT_URL): 'www.thridparty.com']

        when:
        Optional<String> redirectURL = facade.makeSaleRequestForAlternativePayment(cart, 'pcIDEAL', [(PAYMENT_OPTION_ID): 'ideal999'])

        then:
        1 * saleRequester.supports(IDL) >> true
        1 * saleRequester.initiateSale(cart, IDL, 'test_merchant', [(PAYMENT_OPTION_ID): 'ideal999']) >> result
        1 * paymentModeService.getPaymentModeForCode('pcIDEAL') >> pm
        redirectURL.get() == 'www.thridparty.com'
    }

    @Test
    @Unroll
    def 'makeSaleRequestForAlternativePayment: Should return empty value when transaction is not successful'()
    {
        given:
        AlternativePaymentSaleRequester saleRequester = Mock()
        facade.saleRequesters = [saleRequester]

        and:
        def cart = Stub(CartModel)
        def pm = new IsvPaymentModeModel()
        pm.paymentType = ALTERNATIVE_PAYMENT
        pm.paymentSubType = AlternativePaymentMethod.IDL

        and:
        def result = PaymentServiceResult.create()
        def entry = new IsvPaymentTransactionEntryModel()
        result.addData('transaction', entry)
        entry.transactionStatus = transactionStatusParam
        entry.properties = [(PaymentConstants.AlternativePaymentsResponseFields.MERCHANT_URL): merchantUrlParam]

        when:
        Optional<String> redirectURL = facade.makeSaleRequestForAlternativePayment(cart, 'pcIDEAL', [(PAYMENT_OPTION_ID): 'ideal999'])

        then:
        1 * saleRequester.supports(IDL) >> true
        1 * saleRequester.initiateSale(cart, IDL, 'test_merchant', [(PAYMENT_OPTION_ID): 'ideal999']) >> result
        1 * paymentModeService.getPaymentModeForCode('pcIDEAL') >> pm
        redirectURL.isEmpty()

        where:
        transactionStatusParam | merchantUrlParam
        ERROR                  | ''
        ACCEPT                 | ''
        ERROR                  | 'www.fake.com'
    }

    @Test
    @Unroll
    def 'validateAlternativePaymentResponse: Should validate cart if it was paid using concrete alternative payment'()
    {
        given:
        def txn = new IsvPaymentTransactionModel()
        txn.paymentProvider = ALTERNATIVE_PAYMENT.name()
        txn.alternativePaymentMethod = apSubType
        cart.paymentTransactions >> [txn]

        def entry = new IsvPaymentTransactionEntryModel()
        entry.transactionStatus = txnStatus
        entry.type = txnType
        entry.properties = properties
        txn.entries = [entry]

        when:
        def result = facade.validateAlternativePaymentResponse(cart, apSubType.code)

        then:
        result == expectedResult

        where:
        apSubType | properties                                    | txnStatus | txnType                              || expectedResult
        SOF       | [:]                                           | ACCEPT    | PaymentTransactionType.SALE          || true
        SOF       | [:]                                           | REJECT    | PaymentTransactionType.SALE          || false
        APY       | [:]                                           | ACCEPT    | PaymentTransactionType.SALE          || false
        APY       | [:]                                           | ACCEPT    | PaymentTransactionType.INITIATE      || true
        AYM       | [:]                                           | ACCEPT    | PaymentTransactionType.INITIATE      || true
        KLI       | [status: 'REJECTED']                          | ACCEPT    | PaymentTransactionType.AUTHORIZATION || false
        KLI       | [status: 'AUTHORIZED']                        | ACCEPT    | PaymentTransactionType.AUTHORIZATION || true
        KLI       | [status: 'PENDING']                           | ACCEPT    | PaymentTransactionType.AUTHORIZATION || true
        WQR       | [apCheckStatusReplyPaymentStatus: 'SETTLED']  | ACCEPT    | PaymentTransactionType.CHECK_STATUS  || true
        WQR       | [apCheckStatusReplyPaymentStatus: 'REJECTED'] | ACCEPT    | PaymentTransactionType.CHECK_STATUS  || false
    }
}
