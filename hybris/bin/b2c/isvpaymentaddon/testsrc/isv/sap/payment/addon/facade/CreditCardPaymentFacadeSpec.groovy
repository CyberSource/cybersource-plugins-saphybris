package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorfacades.payment.data.PaymentSubscriptionResultData
import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.payment.data.CreateSubscriptionRequest
import de.hybris.platform.acceleratorservices.payment.data.CreateSubscriptionResult
import de.hybris.platform.acceleratorservices.payment.data.CustomerInfoData
import de.hybris.platform.acceleratorservices.payment.data.PaymentData
import de.hybris.platform.acceleratorservices.payment.data.PaymentSubscriptionResultItem
import de.hybris.platform.acceleratorservices.payment.strategies.ClientReferenceLookupStrategy
import de.hybris.platform.acceleratorservices.payment.strategies.CreateSubscriptionRequestStrategy
import de.hybris.platform.acceleratorservices.payment.strategies.CreateSubscriptionResultValidationStrategy
import de.hybris.platform.acceleratorservices.payment.strategies.PaymentResponseInterpretationStrategy
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.commerceservices.enums.CustomerType
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel
import de.hybris.platform.order.CartService
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.site.BaseSiteService
import io.jsonwebtoken.Claims
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.data.enrollment.OrderData
import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.enums.TransactionMode
import isv.cjl.payment.exception.PaymentException
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.jwt.JwtService
import isv.sap.payment.addon.facade.impl.CreditCardPaymentFacadeImpl
import isv.sap.payment.model.IsvPaymentInfoModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.constants.IsvPaymentConstants.ReasonCode.NOT_ENROLLED_CODE

@UnitTest
class CreditCardPaymentFacadeSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    CreateSubscriptionRequestStrategy createSubscriptionRequestStrategy = Mock()
    PaymentResponseInterpretationStrategy paymentResponseInterpretationStrategy = Mock()
    CreateSubscriptionResultValidationStrategy createSubscriptionResultValidationStrategy = Mock()
    SiteConfigService siteConfigService = Mock()
    JwtService jwtService = Mock()

    MerchantService merchantService = Mock()
    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock()
    BaseSiteService baseSiteService = Mock()
    CartService cartService = Mock()
    ModelService modelService = Mock()
    CheckoutCustomerStrategy checkoutCustomerStrategy = Mock()
    PaymentInfoFacade paymentInfoFacade = Mock()
    Converter<CustomerInfoData, AddressModel> creditCardReverseAddressConverter = Mock()
    Converter<CreateSubscriptionRequest, PaymentData> paymentDataConverter = Mock()
    Converter<PaymentSubscriptionResultItem, PaymentSubscriptionResultData> paymentSubscriptionResultDataConverter = Mock()
    Converter<AbstractOrderModel, OrderData> enrollmentPayloadConverter = Mock()
    def customerNameStrategy = Mock(CustomerNameStrategy)
    def clientReferenceLookupStrategy = Mock(ClientReferenceLookupStrategy)
    def paymentTransactionService = Mock(PaymentTransactionService)

    def facade = new CreditCardPaymentFacadeImpl(paymentServiceExecutor: paymentServiceExecutor,
                                                 merchantService: merchantService,
                                                 siteBaseUrlResolutionService: siteBaseUrlResolutionService,
                                                 baseSiteService: baseSiteService,
                                                 cartService: cartService,
                                                 modelService: modelService,
                                                 siteConfigService: siteConfigService,
                                                 jwtService: jwtService,
                                                 paymentDataConverter: paymentDataConverter,
                                                 paymentSubscriptionResultDataConverter: paymentSubscriptionResultDataConverter,
                                                 checkoutCustomerStrategy: checkoutCustomerStrategy,
                                                 customerNameStrategy: customerNameStrategy,
                                                 creditCardReverseAddressConverter: creditCardReverseAddressConverter,
                                                 enrollmentPayloadConverter: enrollmentPayloadConverter,
                                                 createSubscriptionRequestStrategy: createSubscriptionRequestStrategy,
                                                 paymentResponseInterpretationStrategy: paymentResponseInterpretationStrategy,
                                                 clientReferenceLookupStrategy: clientReferenceLookupStrategy,
                                                 createSubscriptionResultValidationStrategy: createSubscriptionResultValidationStrategy,
                                                 paymentInfoFacade: paymentInfoFacade,
                                                 paymentTransactionService:  paymentTransactionService
    )

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    PaymentTransactionModel transaction = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()
    CustomerModel customer = Mock()

    BaseSiteModel site = Mock()
    AddressModel deliveryAddress = Mock()
    AddressModel billingAddress = Mock()
    IsvPaymentInfoModel paymentInfo = Mock()
    CountryModel country = Mock()

    def setup()
    {
        facade.setCheckoutCustomerStrategy(checkoutCustomerStrategy)

        checkoutCustomerStrategy.currentUserForCheckout >> customer

        cart.guid >> 'g-u-i-d'
        cart.paymentTransactions >> []
        cart.deliveryAddress >> deliveryAddress
        cart.paymentInfo >> paymentInfo
        paymentInfo.billingAddress >> billingAddress
        merchantService.getCurrentMerchant(_) >> merchantModel
        merchantModel.id >> 'test_merchant'
        baseSiteService.currentBaseSite >> site

        country.isocode >> 'US'
        deliveryAddress.country >> country
        billingAddress.country >> country

        cartService.sessionCart >> cart
    }

    @Test
    @Unroll
    def 'authorizeFlexCreditCardPayment: Should successfully perform the operation if authorization succeeded'(ccAuthorizationTransactionStatus)
    {
        given:
        def enrollTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollTransaction.properties >> [payerAuthEnrollReplyReasonCode: NOT_ENROLLED_CODE]

        and:
        def authTransaction = Mock(IsvPaymentTransactionEntryModel)
        authTransaction.transactionStatus >> ccAuthorizationTransactionStatus
        paymentTransactionService.createAuthorizationTxEntryFromEnrollment(enrollTransaction) >> Optional.of(authTransaction)

        when:
        def result = facade.authorizeFlexCreditCardPayment(cart, '123456', enrollTransaction)

        then:
        result

        where:
        ccAuthorizationTransactionStatus          | _
        PaymentConstants.TransactionStatus.ACCEPT | _
        PaymentConstants.TransactionStatus.REVIEW | _
    }

    @Test
    def 'authorizeFlexCreditCardPayment: Should throw exception when transaction requires validation'()
    {
        given:
        transactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ACCEPT
        def enrollTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollTransaction.properties >> ['payerAuthEnrollReplyReasonCode': '475']

        when:
        facade.authorizeFlexCreditCardPayment(cart, '123456', enrollTransaction)

        then:
        thrown(PaymentException)
    }

    @Test
    @Unroll
    def 'authorizeFlexCreditCardPayment: Should successfully perform the operation for cart and flex token'(ccAuthorizationTransactionStatus)
    {
        given:
        transactionEntry.transactionStatus >> ccAuthorizationTransactionStatus
        def enrollTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollTransaction.properties >> [:]

        when:
        def result = facade.authorizeFlexCreditCardPayment(cart, '123456')

        then:
        1 * merchantService.getCurrentMerchant(PaymentType.CREDIT_CARD) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkFlexAuthCCRequest(it, cart, '123456')
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)

        and:
        result

        where:
        ccAuthorizationTransactionStatus          | _
        PaymentConstants.TransactionStatus.ACCEPT | _
        PaymentConstants.TransactionStatus.REVIEW | _
    }

    @Test
    @Unroll
    def 'authorizeFlexCreditCardPayment: Should return return false if authorization failed'(ccAuthorizationTransactionStatus)
    {
        given:
        def enrollTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollTransaction.properties >> [payerAuthEnrollReplyReasonCode: NOT_ENROLLED_CODE]

        and:
        def authTransaction = Mock(IsvPaymentTransactionEntryModel)
        authTransaction.transactionStatus >> ccAuthorizationTransactionStatus
        paymentTransactionService.createAuthorizationTxEntryFromEnrollment(enrollTransaction) >> Optional.of(authTransaction)

        when:
        def result = facade.authorizeFlexCreditCardPayment(cart, '123456', enrollTransaction)

        then:
        !result

        0 * modelService.save(transactionEntry)

        where:
        ccAuthorizationTransactionStatus          | _
        PaymentConstants.TransactionStatus.REJECT | _
        PaymentConstants.TransactionStatus.ERROR  | _
    }

    @Test
    def 'authorizeFlexCreditCardPayment: Should return return false if jwt is not valid'()
    {
        given:
        def claims = Mock(Claims)
        jwtService.decodeJwt(_, _) >> claims
        claims.get('Payload') >> ['ErrorNumber': 1]

        when:
        def result = facade.authorizeFlexCreditCardPayment(cart, '123456', 'jwt')

        then:
        0 * merchantService.getCurrentMerchant(PaymentType.CREDIT_CARD) >> merchantModel
        0 * paymentServiceExecutor.execute {
            checkFlexAuthCCRequest(it, cart, '123456')
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)

        !result
    }

    @Test
    @Unroll
    def 'authorizeFlexCreditCardPayment: Should return return transaction result if jwt is valid'(ccAuthorizationTransactionStatus, expected)
    {
        given:
        transactionEntry.transactionStatus >> ccAuthorizationTransactionStatus
        def claims = Mock(Claims)
        jwtService.decodeJwt(_, _) >> claims
        claims.get('Payload') >> ['ErrorNumber': 0, 'Payment': ['ProcessorTransactionId': '123']]

        when:
        def result = facade.authorizeFlexCreditCardPayment(cart, '123456', 'jwt')

        then:
        1 * merchantService.getCurrentMerchant(PaymentType.CREDIT_CARD) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkFlexAuthCCRequest(it, cart, '123456')
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)
        result == expected

        where:
        ccAuthorizationTransactionStatus          | expected
        PaymentConstants.TransactionStatus.ACCEPT | true
        PaymentConstants.TransactionStatus.REVIEW | true
        PaymentConstants.TransactionStatus.REJECT | false
        PaymentConstants.TransactionStatus.ERROR  | false
    }

    @Test
    def 'createEnrollmentJwt should invoke CJL service'()
    {
        given:
        def orderData = new OrderData()

        when:
        facade.createEnrollmentJwt()

        then:
        1 * cartService.sessionCart >> cart
        1 * enrollmentPayloadConverter.convert(cart) >> orderData
        1 * siteConfigService.getProperty('isv.payment.customer.3ds.jwt.api.key') >> '1234'
        1 * jwtService.createEnrollmentJwt('1234', orderData)
    }

    @Test
    def 'begin create payment should return empty data if null subscription'()
    {
        when:
        def result = facade.beginCreatePayment('responseUrl')

        then:
        1 * paymentDataConverter.convert(null) >> null
        result != null
    }

    @Test
    def 'begin create payment should return data from subscription'()
    {
        given:
        def request = new CreateSubscriptionRequest()
        def expected = new PaymentData()

        when:
        def result = facade.beginCreatePayment('responseUrl')

        then:
        1 * createSubscriptionRequestStrategy.createSubscriptionRequest(_, _, _, _, _, _, _) >> request
        1 * paymentDataConverter.convert(request) >> expected
        result == expected
    }

    @Test
    def 'complete create payment should return data from subscription'()
    {
        given:
        customer.paymentInfos >> []
        customer.type >> CustomerType.GUEST
        def subscriptionResult = new CreateSubscriptionResult(customerInfoData: new CustomerInfoData())
        def expectedSubscriptionResultData = new PaymentSubscriptionResultData()

        and:
        modelService.create(AddressModel) >> billingAddress

        when:
        def subscriptionResultData = facade.completeCreatePayment([:], true)

        then:
        1 * paymentResponseInterpretationStrategy.interpretResponse(_, _, _) >> subscriptionResult
        1 * createSubscriptionResultValidationStrategy.validateCreateSubscriptionResult(_, _) >> [:]
        1 * creditCardReverseAddressConverter.convert(subscriptionResult.customerInfoData, billingAddress)
        1 * paymentInfoFacade.createPaymentInfo(billingAddress, customer, true) >> paymentInfo
        1 * paymentSubscriptionResultDataConverter.convert(_) >> expectedSubscriptionResultData

        and:
        subscriptionResultData == expectedSubscriptionResultData
    }

    @Test
    @Unroll
    def 'Should return true or false indicating that 3ds is enabled or not'()
    {
        given:
        merchantService.is3dsEnabled() >> is3dsEnabledParam

        when:
        def enabled = facade.is3dsEnabled()

        then:
        enabled == is3dsEnabledParam

        where:
        is3dsEnabledParam | _
        true              | _
        false             | _
    }

    def checkFlexAuthCCRequest(PaymentServiceRequest request, cart, flexToken)
    {
        request.paymentType == PaymentType.CREDIT_CARD &&
                request.paymentTransactionType == PaymentTransactionType.AUTHORIZATION &&
                request.getParam('order').is(cart) &&
                request.getParam('merchantId') == 'test_merchant' &&
                request.getParam('flexToken') == flexToken
    }

    @Test
    def 'enrollCreditCard: Should successfully process credit card enrollment'()
    {
        given:
        def referenceId = 'reference12345'
        def transientToken = 'transient12345'

        when:
        def result = facade.enrollCreditCard(referenceId, transientToken)

        then:
        1 * merchantService.getCurrentMerchant(PaymentType.CREDIT_CARD) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkEnrollmentCCRequest(it, cart, referenceId, transientToken)
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)

        result == transactionEntry
    }

    def checkEnrollmentCCRequest(PaymentServiceRequest request, cart, referenceId, transientToken)
    {
        request.paymentType == PaymentType.CREDIT_CARD &&
                request.paymentTransactionType == PaymentTransactionType.ENROLLMENT &&
                request.getParam('payerAuthEnrollServiceTransactionMode') == TransactionMode.ECOMMERCE &&
                request.getParam('order').is(cart) &&
                request.getParam('merchantId') == 'test_merchant' &&
                request.getParam('flexToken') == transientToken &&
                request.getParam('payerAuthEnrollServiceReferenceID') == referenceId
    }
}
