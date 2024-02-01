package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.site.BaseSiteService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.applepay.ApplePayDecryptionService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.utils.PaymentHelper
import isv.sap.payment.addon.facade.impl.ApplePayPaymentFacadeImpl
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_DECRYPTION_TYPE
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_INITIATIVE
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_INITIATIVE_CONTEXT
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_KEYSTORE_LOCATION
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_KEYSTORE_PASSWORD
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_MERCHANT_IDENTIFIER

@UnitTest
class ApplePayPaymentFacadeImplSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    MerchantService merchantService = Mock()

    BaseSiteService baseSiteService = Mock()
    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock()
    SiteConfigService siteConfigService = Mock()

    ApplePayDecryptionService applePayDecryptionService = Mock()
    PaymentHelper paymentHelper = Mock()

    ApplePayPaymentFacadeImpl facade = Spy()

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()

    BaseSiteModel site = Mock()

    def paymentServiceResult = PaymentServiceResult.create()

    def setup()
    {
        facade.merchantService = merchantService
        facade.paymentServiceExecutor = paymentServiceExecutor
        facade.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        facade.siteConfigService = siteConfigService
        facade.baseSiteService = baseSiteService

        facade.applePayDecryptionService = applePayDecryptionService
        facade.paymentHelper = paymentHelper

        merchantService.getCurrentMerchant(_) >> merchantModel
        merchantModel.id >> 'test_merchant'
        baseSiteService.currentBaseSite >> site
    }

    @Test
    def 'createApplePaySession: Should return ApplePay session'()
    {
        given:
        paymentServiceResult.addData(PaymentConstants.ApplePay.CreateSession.SESSION, ['applePaySession': 'value'])
        site.name >> 'test'
        site.uid >> 'test-store'

        when:
        def applePaySession = facade.createApplePaySession('validationUrl')

        then:
        1 * paymentServiceExecutor.execute(_) >> paymentServiceResult
        1 * siteConfigService.getProperty(APPLE_PAY_MERCHANT_IDENTIFIER)
        1 * siteConfigService.getProperty(APPLE_PAY_INITIATIVE)
        1 * siteConfigService.getProperty(String.format(APPLE_PAY_INITIATIVE_CONTEXT, 'test-store'))
        1 * siteConfigService.getProperty(APPLE_PAY_KEYSTORE_LOCATION)
        1 * siteConfigService.getProperty(APPLE_PAY_KEYSTORE_PASSWORD)
        applePaySession == ['applePaySession': 'value']
    }

    @Test
    def 'authorizeApplePayPayment: Should do ApplePay authorization using isv decryption'()
    {
        given:
        def paymentToken = [
                'token': [
                        'paymentData'          : [
                                'version'  : 'EC_v1',
                                'data'     : 'U86HMLZiSP0WzkUAO8/Su48QZ+575zmFBTBqfwQZOeNzzRi3SXb2DRTzqrEEBkxJUv1XhLHqc4Duo1JMul7WPx3VeEbfTBYuxKXEZv4JcVJOyFKqHWcR2/byBPdM914OAGPl8YFREPyb9KFmr8k/meEnZ14P19RixKzCjntW7UN9NBxlQB1K9CFgHOs7iZpEQ+SGgWUwJeNWkjYKUOZOWNp1smdPtZBGaihzGjQDipv1imrW6NIaj9JN7qLc2E/vcG77601bk0u/KKi2RI/XZFu+QYyaPwMR9sLTv58DMHu5mY5H6zc0Y3yS7ivR5oJfXa1m2e02aEKeOgTKuY+mX2qArH+5NEj0yb72Pk9+JFBn6W+u+B7ykIBoIhUZj/PKFHtR2SlbSaTe/4iF0TDgy4HHoWn38uNMZzWKlpL0xms=',
                                'signature': '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',
                                'header'   : [
                                        'ephemeralPublicKey': 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERbaKKG8oSGBrwgHho4jO2loS04VwXLHYTnwHbGr2oen2W7RB3kG7Ala5fe7xJ9xt4ujmDJdq8OyvTdoSLSaGag==',
                                        'publicKeyHash'     : 'zGiLsw0A1LRn42eW+MWAhLswJguUG5FJZYQ5264k7S0=',
                                        'transactionId'     : 'a1d07b1df7aaf4282c3e29c67ec04c96867b046ef18c870124654a66f9e7f4a5'
                                ]
                        ],
                        'paymentMethod'        : [
                                'displayName': 'Visa 0492',
                                'network'    : 'Visa',
                                'type'       : 'debit'
                        ],
                        'transactionIdentifier': 'A1D07B1DF7AAF4282C3E29C67EC04C96867B046EF18C870124654A66F9E7F4A5']]
        transactionEntry.transactionStatus >> transactionStatus

        when:
        def result = facade.authorizeApplePayPayment(paymentToken, cart)

        then:
        1 * paymentServiceExecutor.execute(_) >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)
        1 * siteConfigService.getProperty(APPLE_PAY_DECRYPTION_TYPE) >> 'ISV_PAYMENT'
        0 * facade.decryptApplePaymentData(_)
        result == expectedResult

        where:
        transactionStatus                                    | expectedResult
        PaymentConstants.TransactionStatus.ACCEPT | true
        PaymentConstants.TransactionStatus.REJECT | false
    }

    @Test
    def 'authorizeApplePayPayment: Should do ApplePay authorization using merchant decryption'()
    {
        given:
        def paymentToken = [
                'token': [
                        'paymentData'          : [
                                'version'  : 'EC_v1',
                                'data'     : 'U86HMLZiSP0WzkUAO8/Su48QZ+575zmFBTBqfwQZOeNzzRi3SXb2DRTzqrEEBkxJUv1XhLHqc4Duo1JMul7WPx3VeEbfTBYuxKXEZv4JcVJOyFKqHWcR2/byBPdM914OAGPl8YFREPyb9KFmr8k/meEnZ14P19RixKzCjntW7UN9NBxlQB1K9CFgHOs7iZpEQ+SGgWUwJeNWkjYKUOZOWNp1smdPtZBGaihzGjQDipv1imrW6NIaj9JN7qLc2E/vcG77601bk0u/KKi2RI/XZFu+QYyaPwMR9sLTv58DMHu5mY5H6zc0Y3yS7ivR5oJfXa1m2e02aEKeOgTKuY+mX2qArH+5NEj0yb72Pk9+JFBn6W+u+B7ykIBoIhUZj/PKFHtR2SlbSaTe/4iF0TDgy4HHoWn38uNMZzWKlpL0xms=',
                                'signature': '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',
                                'header'   : [
                                        'ephemeralPublicKey': 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERbaKKG8oSGBrwgHho4jO2loS04VwXLHYTnwHbGr2oen2W7RB3kG7Ala5fe7xJ9xt4ujmDJdq8OyvTdoSLSaGag==',
                                        'publicKeyHash'     : 'zGiLsw0A1LRn42eW+MWAhLswJguUG5FJZYQ5264k7S0=',
                                        'transactionId'     : 'a1d07b1df7aaf4282c3e29c67ec04c96867b046ef18c870124654a66f9e7f4a5'
                                ]
                        ],
                        'paymentMethod'        : [
                                'displayName': 'Visa 0492',
                                'network'    : 'Visa',
                                'type'       : 'debit'
                        ],
                        'transactionIdentifier': 'A1D07B1DF7AAF4282C3E29C67EC04C96867B046EF18C870124654A66F9E7F4A5']]
        transactionEntry.transactionStatus >> transactionStatus

        when:
        def result = facade.authorizeApplePayPayment(paymentToken, cart)

        then:
        1 * paymentServiceExecutor.execute(_) >> PaymentServiceResult.create().addData(TRANSACTION, transactionEntry)
        1 * siteConfigService.getProperty(APPLE_PAY_DECRYPTION_TYPE) >> 'MERCHANT'
        1 * facade.decryptApplePaymentData(_) >> ['token': ['data': 'value']]
        result == expectedResult

        where:
        transactionStatus                                    | expectedResult
        PaymentConstants.TransactionStatus.ACCEPT | true
        PaymentConstants.TransactionStatus.REJECT | false
    }

    @Test
    def 'decryptApplePaymentData: Should call decryption service'()
    {
        given:
        def paymentToken = [
                'token': [
                        'paymentData'          : [
                                'version'  : 'EC_v1',
                                'data'     : 'U86HMLZiSP0WzkUAO8/Su48QZ+575zmFBTBqfwQZOeNzzRi3SXb2DRTzqrEEBkxJUv1XhLHqc4Duo1JMul7WPx3VeEbfTBYuxKXEZv4JcVJOyFKqHWcR2/byBPdM914OAGPl8YFREPyb9KFmr8k/meEnZ14P19RixKzCjntW7UN9NBxlQB1K9CFgHOs7iZpEQ+SGgWUwJeNWkjYKUOZOWNp1smdPtZBGaihzGjQDipv1imrW6NIaj9JN7qLc2E/vcG77601bk0u/KKi2RI/XZFu+QYyaPwMR9sLTv58DMHu5mY5H6zc0Y3yS7ivR5oJfXa1m2e02aEKeOgTKuY+mX2qArH+5NEj0yb72Pk9+JFBn6W+u+B7ykIBoIhUZj/PKFHtR2SlbSaTe/4iF0TDgy4HHoWn38uNMZzWKlpL0xms=',
                                'signature': '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',
                                'header'   : [
                                        'ephemeralPublicKey': 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERbaKKG8oSGBrwgHho4jO2loS04VwXLHYTnwHbGr2oen2W7RB3kG7Ala5fe7xJ9xt4ujmDJdq8OyvTdoSLSaGag==',
                                        'publicKeyHash'     : 'zGiLsw0A1LRn42eW+MWAhLswJguUG5FJZYQ5264k7S0=',
                                        'transactionId'     : 'a1d07b1df7aaf4282c3e29c67ec04c96867b046ef18c870124654a66f9e7f4a5'
                                ]
                        ],
                        'paymentMethod'        : [
                                'displayName': 'Visa 0492',
                                'network'    : 'Visa',
                                'type'       : 'debit'
                        ],
                        'transactionIdentifier': 'A1D07B1DF7AAF4282C3E29C67EC04C96867B046EF18C870124654A66F9E7F4A5']]

        when:
        facade.decryptApplePaymentData(paymentToken)

        then:
        1 * applePayDecryptionService.decrypt(paymentToken)
    }

}
