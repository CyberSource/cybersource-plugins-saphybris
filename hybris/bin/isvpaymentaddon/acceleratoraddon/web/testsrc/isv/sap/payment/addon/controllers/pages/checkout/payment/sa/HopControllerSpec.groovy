package isv.sap.payment.addon.controllers.pages.checkout.payment.sa

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.servicelayer.i18n.I18NService
import de.hybris.platform.site.BaseSiteService
import org.apache.commons.configuration.BaseConfiguration
import org.apache.commons.configuration.Configuration
import org.junit.Test
import org.springframework.ui.Model
import spock.lang.Specification

import isv.cjl.payment.model.Merchant
import isv.cjl.payment.model.MerchantProfile
import isv.cjl.payment.security.service.SAService
import isv.cjl.payment.service.MerchantService
import isv.sap.payment.addon.provider.SecureAcceptanceUrlProvider
import isv.sap.payment.data.PaymentSystemInfo
import isv.sap.payment.enums.MerchantProfileType

@UnitTest
class HopControllerSpec extends Specification
{
    def sAService = Mock([useObjenesis: false], SAService)

    def siteService = Mock([useObjenesis: false], BaseSiteService)

    def i18NService = Mock([useObjenesis: false], I18NService)

    def siteBaseUrlResolutionService = Mock([useObjenesis: false], SiteBaseUrlResolutionService)

    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    def cartService = Mock([useObjenesis: false], CartService)

    def merchantService = Mock([useObjenesis: false], MerchantService)

    def model = Mock([useObjenesis: false], Model)

    def currentSite = Mock([useObjenesis: false], BaseSiteModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def paymentSystemInfo = Mock([useObjenesis: false], PaymentSystemInfo)

    def merchant = Mock([useObjenesis: false], Merchant)

    def profile = Mock([useObjenesis: false], MerchantProfile)

    def cart = Mock([useObjenesis: false], CartModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def configuration = Mock([useObjenesis: false], Configuration)

    def currentLocale = Locale.ENGLISH

    def hopSecureAcceptanceUrlProvider = Mock([useObjenesis: false], SecureAcceptanceUrlProvider)

    def controller = new HopController()

    def setup()
    {
        controller.sAService = sAService
        controller.siteService = siteService
        controller.i18NService = i18NService
        controller.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        controller.configurationService = configurationService
        controller.cartService = cartService
        controller.merchantService = merchantService
        controller.paymentSystemInfo = paymentSystemInfo

        currentSite.uid >> 'site'
        siteService.currentBaseSite >> currentSite

        merchant.id >> 'MERCHANT1'
        merchant.profiles >> [profile]

        profile.profileId >> 'merchantProfileId'
        profile.profileType >> MerchantProfileType.HOP
        profile.accessKey >> 'accessKey'
        profile.secretKey >> 'securityKey'
        profile.merchant >> merchant

        merchantService.getCurrentMerchantProfile(isv.cjl.payment.enums.MerchantProfileType.HOP) >> profile

        cartService.sessionCart >> cart

        cart.paymentInfo >> paymentInfo
        cart.totalPrice >> 200D
        cart.currency >> currency
        cart.fingerPrintSessionID >> 'device_fingerprint_id'
        cart.guid >> '1234567890'

        currency.isocode >> 'USD'
        region.isocodeShort >> 'CA'

        paymentInfo.billingAddress >> billingAddress

        billingAddress.lastname >> 'Smith'
        billingAddress.firstname >> 'John'
        billingAddress.email >> 'jsmith@mail.com'
        billingAddress.town >> 'San Francisco'
        billingAddress.streetname >> 'Embarcadero'
        billingAddress.line1 >> '1'
        billingAddress.line2 >> '2'
        billingAddress.country >> country
        billingAddress.region >> region
        billingAddress.postalcode >> '94111'

        country.isocode >> 'US'

        i18NService.currentLocale >> currentLocale

        configurationService.configuration >> baseConfig()

        siteBaseUrlResolutionService.getWebsiteUrlForSite(currentSite, true, '/checkout/payment/sa/receipt') >> 'https://apparel-uk/checkout/payment/sa/receipt'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(currentSite, true, '/checkout/payment/sa/merchantpost') >> 'https://apparel-uk/checkout/payment/sa/merchantpost'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(currentSite, true, '/checkout/multi/summary/payment/canceled') >> 'https://apparel-uk/checkout/payment/sa/cancel'

        sAService.getDigest(_, 'securityKey') >> 'signed_data'

        paymentSystemInfo.partnerSolutionID >> 'partnerSolutionID'
        paymentSystemInfo.developerID >> 'developerID'

        hopSecureAcceptanceUrlProvider.getURL() >> 'secure_acceptance_form_url'

        controller.hopSecureAcceptanceUrlProvider = hopSecureAcceptanceUrlProvider
    }

    @Test
    def 'controller should render hopRequestForm'()
    {
        when:
        def result = controller.sendPaymentRequest(model)

        then:
        result == 'addon:/isvpaymentaddon/pages/checkout/payment/sa/hopRequest'
    }

    @Test
    def 'controller should send valid request params'()
    {
        when:
        controller.sendPaymentRequest(model)

        then:
        model.addAttribute('isvRequest', *_) >> { args ->
            def map = args[1] as Map<String, Object>

            assert map.access_key == 'accessKey'
            assert map.profile_id == 'merchantProfileId'
            assert map.profile_id == 'merchantProfileId'
            assert map.transaction_uuid =~ /.{8}-.{4}-.{4}-.{4}-.{12}/
            assert map.signed_field_names == 'access_key,profile_id,transaction_uuid,signed_field_names,unsigned_field_names,signed_date_time,locale,transaction_type,reference_number,amount,currency,bill_to_forename,bill_to_surname,bill_to_email,bill_to_address_country,bill_to_address_city,bill_to_address_line1,bill_to_address_line2,bill_to_address_postal_code,override_custom_receipt_page,device_fingerprint_id'
            assert map.unsigned_field_names == 'merchant_defined_data99,merchant_defined_data100'
            assert map.signed_date_time =~ /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/
            assert map.locale == 'en'
            assert map.transaction_type == 'authorization'
            assert map.reference_number == '1234567890'
            assert map.amount == '200.0'
            assert map.currency == 'USD'
            assert map.bill_to_forename == 'John'
            assert map.bill_to_surname == 'Smith'
            assert map.bill_to_email == 'jsmith@mail.com'
            assert map.bill_to_address_country == 'US'
            assert map.bill_to_address_state == 'CA'
            assert map.bill_to_address_city == 'San Francisco'
            assert map.bill_to_address_line1 == 'Embarcadero'
            assert map.bill_to_address_line2 == '1'
            assert map.bill_to_address_postal_code == '94111'
            assert map.override_custom_receipt_page == 'https://apparel-uk/checkout/payment/sa/receipt'
            assert map.override_custom_cancel_page == 'https://apparel-uk/checkout/payment/sa/cancel'
            assert map.hop_url == 'secure_acceptance_form_url'
            assert map.signature == 'signed_data'
            assert map.merchant_defined_data99 == 'MERCHANT1'
            assert map.merchant_defined_data100 == 'hop'
            assert map.device_fingerprint_id == 'device_fingerprint_id'
            assert map.partner_solution_id == 'partner_solution_id'
            assert map.developer_id == 'developer_id'
        }
    }

    private Configuration baseConfig()
    {
        def config = new BaseConfiguration()

        config.addProperty('isv.secure.acceptance.hop.post.url', 'https://testsecureacceptance.isv.com/pay')
        config.addProperty('secure.acceptance.site.transaction.type', 'authorization')

        config
    }
}
