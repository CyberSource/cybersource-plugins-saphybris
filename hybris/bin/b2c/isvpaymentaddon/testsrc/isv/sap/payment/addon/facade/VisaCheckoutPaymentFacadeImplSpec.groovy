package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade
import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.commercefacades.order.data.DeliveryModeData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel
import de.hybris.platform.order.CartService
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.i18n.CommonI18NService
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.site.BaseSiteService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.addon.facade.impl.VisaCheckoutPaymentFacadeImpl
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION
import static isv.cjl.payment.enums.PaymentTransactionType.GET
import static isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT

@UnitTest
class VisaCheckoutPaymentFacadeImplSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    PaymentTransactionService paymentTransactionService = Mock()
    MerchantService merchantService = Mock()

    ModelService modelService = Mock()
    BaseSiteService baseSiteService = Mock()
    CartService cartService = Mock()
    CommonI18NService commonI18NService = Mock()
    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock()
    CheckoutCustomerStrategy checkoutCustomerStrategy = Mock()
    SiteConfigService siteConfigService = Mock()
    AcceleratorCheckoutFacade checkoutFacade = Mock()
    PaymentInfoFacade paymentInfoFacade = Mock()
    Converter<Map<String, String>, AddressModel> visaCheckoutBillingAddressReverseConverter = Mock()
    Converter<Map<String, String>, AddressModel> visaCheckoutDeliveryAddressReverseConverter = Mock()

    VisaCheckoutPaymentFacadeImpl facade = Spy(new VisaCheckoutPaymentFacadeImpl(
            merchantService: merchantService,
            paymentServiceExecutor: paymentServiceExecutor,
            paymentTransactionService: paymentTransactionService,
            siteBaseUrlResolutionService: siteBaseUrlResolutionService,
            siteConfigService: siteConfigService,
            baseSiteService: baseSiteService,
            modelService: modelService,
            checkoutFacade: checkoutFacade,
            paymentInfoFacade: paymentInfoFacade,
            checkoutCustomerStrategy: checkoutCustomerStrategy,
            visaCheckoutBillingAddressReverseConverter: visaCheckoutBillingAddressReverseConverter,
            visaCheckoutDeliveryAddressReverseConverter: visaCheckoutDeliveryAddressReverseConverter
    ))

    PaymentTransactionModel transaction = Mock()

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()

    BaseSiteModel site = Mock()
    AddressModel deliveryAddress = Mock()
    AddressModel billingAddress = Mock()
    PaymentInfoModel paymentInfo = Mock()
    CountryModel country = Mock()
    RegionModel region = Mock()
    CustomerModel customer = Mock()

    def setup()
    {
        checkoutCustomerStrategy.currentUserForCheckout >> customer
        paymentInfoFacade.resolvePaymentInfo(cart, customer) >> paymentInfo

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
        commonI18NService.getCountry('US') >> country
        commonI18NService.getRegion(country, 'US-NY') >> region

        cartService.sessionCart >> cart
    }

    @Test
    def 'authorizeVisaCheckoutPayment: Should successfully perform the operation'(vcAuthorizationTransactionStatus)
    {
        given:
        transactionEntry.transactionStatus >> vcAuthorizationTransactionStatus

        when:
        def result = facade.authorizeVisaCheckoutPayment(cart, '123456')

        then:
        1 * merchantService.getCurrentMerchant(VISA_CHECKOUT) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkVCRequest(it, AUTHORIZATION, cart)
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)

        result

        1 * paymentTransactionService.addProperty('vcOrderId', '123456', transactionEntry)

        where:
        vcAuthorizationTransactionStatus                     | _
        PaymentConstants.TransactionStatus.ACCEPT | _
        PaymentConstants.TransactionStatus.REVIEW | _
    }

    @Test
    def 'authorizeVisaCheckoutPayment: Should return false performing the operation'(vcAuthorizationTransactionStatus)
    {
        given:
        transactionEntry.transactionStatus >> vcAuthorizationTransactionStatus

        when:
        def result = facade.authorizeVisaCheckoutPayment(cart, '123456')

        then:
        1 * merchantService.getCurrentMerchant(VISA_CHECKOUT) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkVCRequest(it, AUTHORIZATION, cart)
        } >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)

        !result

        0 * transactionEntry.setProperties(_)
        0 * modelService.save(transactionEntry)

        where:
        vcAuthorizationTransactionStatus                     | _
        PaymentConstants.TransactionStatus.REJECT | _
        PaymentConstants.TransactionStatus.ERROR  | _
    }

    @Test
    def 'authorizeVisaCheckoutPayment: Should authorize by performing get operation first'()
    {
        given:
        facade.updateCartAddressesWithVCGetData(cart, '123456') >> getResult
        facade.authorizeVisaCheckoutPayment(cart, '123456') >> true

        when:
        def result = facade.authorizeVisaCheckoutPayment(cart, '123456', true)

        then:
        result == success

        where:
        getResult | success
        true      | true
        false     | false
    }

    @Test
    def 'updateCartAddressesWithVCGetData: Should successfully perform the operation'(vcGetTransactionStatus)
    {
        given:
        transactionEntry.transactionStatus >> vcGetTransactionStatus

        def vcGetTransactionEntry = Mock(IsvPaymentTransactionEntryModel)
        vcGetTransactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ACCEPT
        vcGetTransactionEntry.properties >> [:]

        when:
        def result = facade.updateCartAddressesWithVCGetData(cart, '123456')

        then:
        1 * merchantService.getCurrentMerchant(VISA_CHECKOUT) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkVCRequest(it, GET, cart)
        } >> PaymentServiceResult.create().addData(TRANSACTION, vcGetTransactionEntry)
        1 * visaCheckoutDeliveryAddressReverseConverter.convert(vcGetTransactionEntry.properties, deliveryAddress)
        1 * visaCheckoutBillingAddressReverseConverter.convert(vcGetTransactionEntry.properties, billingAddress)
        1 * facade.setDeliveryModeIfNecessary(cart) >> null
        1 * modelService.saveAll(deliveryAddress, billingAddress)

        and:
        result

        where:
        vcGetTransactionStatus                    | _
        PaymentConstants.TransactionStatus.ACCEPT | _
        PaymentConstants.TransactionStatus.REVIEW | _
    }

    @Test
    def 'updateCartAddressesWithVCGetData: Should return false performing the operation'(vcGetTransactionStatus)
    {
        given:
        def vcGetTransactionEntry = Mock(IsvPaymentTransactionEntryModel)
        vcGetTransactionEntry.transactionStatus >> vcGetTransactionStatus
        vcGetTransactionEntry.properties >> [:]

        when:
        def result = facade.updateCartAddressesWithVCGetData(cart, '123456')

        then:
        1 * merchantService.getCurrentMerchant(VISA_CHECKOUT) >> merchantModel
        1 * paymentServiceExecutor.execute {
            checkVCRequest(it, GET, cart)
        } >> PaymentServiceResult.create().addData(TRANSACTION, vcGetTransactionEntry)

        !result

        0 * transactionEntry.setProperties(_)
        0 * modelService.save(transactionEntry)

        where:
        vcGetTransactionStatus                               | _
        PaymentConstants.TransactionStatus.REJECT | _
        PaymentConstants.TransactionStatus.ERROR  | _
    }

    @Test
    def 'Should set delivery mode'()
    {
        given:
        def deliveryModeData = Mock(DeliveryModeData)

        checkoutFacade.supportedDeliveryModes >> [deliveryModeData]
        cart.deliveryMode >> null
        deliveryModeData.code >> 'code'

        when:
        facade.setDeliveryModeIfNecessary(cart)

        then:
        1 * checkoutFacade.setDeliveryMode('code')
    }

    @Test
    def 'Should not set delivery mode if already existing'()
    {
        given:
        def deliveryMode = Mock(DeliveryModeModel)

        cart.deliveryMode >> deliveryMode

        when:
        facade.setDeliveryModeIfNecessary(cart)

        then:
        0 * checkoutFacade.setDeliveryMode(_)
    }

    def checkVCRequest(PaymentServiceRequest request, txnType, cart)
    {
        request.paymentType == VISA_CHECKOUT &&
                request.paymentTransactionType == txnType &&
                request.getParam('order').is(cart) &&
                request.getParam('merchantId') == 'test_merchant' &&
                request.getParam('vcOrderId') == '123456'
    }
}
