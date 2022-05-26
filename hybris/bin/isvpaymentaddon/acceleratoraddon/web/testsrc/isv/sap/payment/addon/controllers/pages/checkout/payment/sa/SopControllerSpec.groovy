package isv.sap.payment.addon.controllers.pages.checkout.payment.sa

import java.util.function.Function

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
import org.springframework.ui.ModelMap
import spock.lang.Specification

import isv.cjl.payment.model.Merchant
import isv.cjl.payment.model.MerchantProfile
import isv.cjl.payment.security.service.SAService
import isv.cjl.payment.service.MerchantService
import isv.sap.payment.data.PaymentSystemInfo

import static SopController.POST_URL
import static SopController.SOP_REQUEST
import static isv.cjl.payment.enums.MerchantProfileType.SOP
import static java.util.Locale.UK
import static org.apache.commons.lang.StringUtils.EMPTY

@UnitTest
class SopControllerSpec extends Specification
{
    def controller = new SopController()

    def cartService = Mock([useObjenesis: false], CartService)

    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    def i18NService = Mock([useObjenesis: false], I18NService)

    def merchantService = Mock([useObjenesis: false], MerchantService)

    def siteBaseUrlResolutionService = Mock([useObjenesis: false], SiteBaseUrlResolutionService)

    def siteService = Mock([useObjenesis: false], BaseSiteService)

    def sAService = Mock([useObjenesis: false], SAService)

    def 'setup'()
    {
        def site = new BaseSiteModel(uid: 'site')

        configurationService.configuration >> configuration()
        cartService.sessionCart >> cart()
        merchantService.getCurrentMerchantProfile(SOP) >> profile()
        i18NService.currentLocale >> UK

        siteService.currentBaseSite >> site
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/checkout/payment/sa/receipt') >> 'receiptUrl'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, '/checkout/payment/sa/merchantpost') >> 'merchantPostUrl'
        sAService.getDigest(_, 'secretKey') >> Mock([useObjenesis: false], Function)

        controller.cartService = cartService
        controller.configurationService = configurationService
        controller.merchantService = merchantService
        controller.i18NService = i18NService
        controller.siteService = siteService
        controller.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        controller.sAService = sAService

        controller.paymentSystemInfo = new PaymentSystemInfo(developerID: 'developerID', partnerSolutionId: 'D2OKBRXN')
    }

    @Test
    def 'should return sop form'()
    {
        given:
        def model = new ModelMap()

        when:
        def actual = controller.getSopForm(model)
        def fields = model['formFields']

        then:
        actual == SOP_REQUEST

        model['cartGuid'] == 'cartGuid'
        model['postUrl'] == 'postUrl'

        fields['access_key'] == 'accessKey'
        fields['profile_id'] == 'profileId'
        fields['transaction_uuid'] != null
        fields['signed_date_time'] != null
        fields['locale'] == 'en_GB'
        fields['transaction_type'] == 'create_subscription'
        fields['reference_number'] == 'cartGuid'
        fields['amount'] == '100.0'
        fields['currency'] == 'GBP'
        fields['payment_method'] == 'card'
        fields['bill_to_forename'] == 'firstName'
        fields['bill_to_surname'] == 'lastName'
        fields['bill_to_email'] == 'email'
        fields['bill_to_address_line1'] == 'line1'
        fields['bill_to_address_city'] == 'town'
        fields['bill_to_address_country'] == 'GB'
        fields['bill_to_address_state'] == 'LN'
        fields['override_custom_receipt_page'] == 'receiptUrl'
        fields['override_backoffice_post_url'] == 'merchantPostUrl'
        fields['device_fingerprint_id'] == 'fingerprintID'
        fields['partner_solution_id'] == 'D2OKBRXN'
        fields['developer_id'] == 'developerID'
        fields['merchant_defined_data99'] == 'merchantId'
        fields['merchant_defined_data100'] == 'SOP'
        fields['card_type_selection_indicator'] == '1'
        fields['signed_field_names'] == 'access_key,profile_id,transaction_uuid,signed_date_time,locale,transaction_type,reference_number,amount,currency,payment_method,bill_to_forename,bill_to_surname,bill_to_email,bill_to_address_line1,bill_to_address_city,bill_to_address_country,bill_to_address_postal_code,bill_to_address_state,override_custom_receipt_page,override_backoffice_post_url,device_fingerprint_id,unsigned_field_names,signed_field_names'
        fields['unsigned_field_names'] == 'partner_solution_id,developer_id,card_type,card_number,card_expiry_date,card_cvn,merchant_defined_data99,merchant_defined_data100,card_type_selection_indicator'
        fields['card_type'] == EMPTY
        fields['card_number'] == EMPTY
        fields['card_number'] == EMPTY
        fields['card_expiry_date'] == EMPTY
        fields['card_cvn'] == EMPTY
    }

    private CartModel cart()
    {
        def address = Mock([useObjenesis: false], AddressModel)

        address.firstname >> 'firstName'
        address.lastname >> 'lastName'
        address.email >> 'email'
        address.line1 >> 'line1'
        address.town >> 'town'
        address.region >> new RegionModel(isocodeShort: 'LN')
        address.country >> new CountryModel(isocode: 'GB')
        address.postalcode >> 'postalCode'

        def paymentInfo = new PaymentInfoModel(billingAddress: address)

        new CartModel(guid: 'cartGuid', paymentInfo: paymentInfo, fingerPrintSessionID: 'fingerprintID',
                      totalPrice: 100D, currency: new CurrencyModel(isocode: 'GBP'))
    }

    private Configuration configuration()
    {
        def configuration = new BaseConfiguration()

        configuration.addProperty(POST_URL, 'postUrl')
        configuration.addProperty('secure.acceptance.site.transaction.type', 'create_subscription')

        configuration
    }

    private MerchantProfile profile()
    {
        def merchant = new Merchant(id: 'merchantId')

        new MerchantProfile(secretKey: 'secretKey', accessKey: 'accessKey', profileId: 'profileId',
                                merchant: merchant)
    }
}
