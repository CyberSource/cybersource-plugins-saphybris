package isv.sap.payment.addon.b2c.controllers.pages.checkout.steps

import java.lang.reflect.Field

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController
import de.hybris.platform.cms2.model.site.CMSSiteModel
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.site.BaseSiteService
import org.apache.commons.configuration.Configuration
import org.junit.Test
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import spock.lang.Specification

import isv.cjl.payment.enums.MerchantProfileType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.model.MerchantProfile
import isv.cjl.payment.service.MerchantService
import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData
import isv.sap.payment.addon.facade.PaymentModeFacade
import isv.sap.payment.addon.facade.VisaCheckoutPaymentDetailsFacade
import isv.sap.payment.enums.AlternativePaymentMethod

import static isv.cjl.payment.enums.PaymentType.GOOGLE_PAY
import static isv.sap.payment.enums.AlternativePaymentMethod.GGP
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT

@UnitTest
class SummaryCheckoutStepControllerSpec extends Specification
{
    SummaryCheckoutStepController controller = new SummaryCheckoutStepController()

    PaymentModeFacade paymentModeFacade = Mock()

    MerchantService merchantService = Mock()

    Model model = new ExtendedModelMap()

    MerchantProfile profile = new MerchantProfile()

    ConfigurationService configurationService = Mock()

    Configuration configuration = Mock()

    BaseSiteService baseSiteService = Mock()

    CMSSiteModel site = Mock()

    def vcPaymentDetailsFacade = Mock(VisaCheckoutPaymentDetailsFacade)

    def setup()
    {
        controller.paymentModeFacade = paymentModeFacade
        controller.merchantService = merchantService
        merchantService.
                getMerchantProfileForPaymentType(PaymentType.VISA_CHECKOUT, MerchantProfileType.VCO) >> profile
        profile.accessKey = '12345'
        controller.klarnaSDKUrl = 'http:klarna'
        controller.visaCheckoutPaymentDetailsFacade = vcPaymentDetailsFacade
        configurationService.configuration >> configuration
        baseSiteService.currentBaseSite >> site

        Field field = AbstractPageController.getDeclaredField('baseSiteService')
        field.setAccessible(true)
        field.set(controller, baseSiteService)

        controller.googlePayMerchantId = '1234'
        controller.googlePayEnvironment = 'TEST'
    }

    @Test
    def 'prepareVisaCheckoutData: should not setup visacheckout data as such payment method is not available'()
    {
        when:
        controller.prepareVisaCheckoutData(model, 'visaImageURL', 'visaSDKURL')

        then:
        1 * paymentModeFacade.isPaymentModeSupported(isv.sap.payment.enums.PaymentType.VISA_CHECKOUT, null) >> false

        with(model.asMap()) {
            get('visaCheckoutEnabled') != true
            get('visaCheckoutImageUrl') == null
            get('visaCheckoutSDKUrl') == null
            get('visaCheckoutAPIKey') == null
        }
    }

    @SuppressWarnings('UnnecessaryGetter')
    @Test
    def 'prepareVisaCheckoutData: should setup visacheckout data as such payment method is available'()
    {
        given:
        def vcPaymentDetailsData = Mock(VisaCheckoutPaymentDetailsData)

        when:
        controller.prepareVisaCheckoutData(model, 'visaImageURL', 'visaSDKURL')

        then:
        1 * paymentModeFacade.isPaymentModeSupported(isv.sap.payment.enums.PaymentType.VISA_CHECKOUT, null) >> true
        1 * vcPaymentDetailsFacade.getVCPaymentDetails() >> Optional.of(vcPaymentDetailsData)

        with(model.asMap()) {
            get('visaCheckoutEnabled')
            get('visaCheckoutImageUrl') == 'visaImageURL'
            get('visaCheckoutSDKUrl') == 'visaSDKURL'
            get('visaCheckoutAPIKey') == '12345'
            get('visaCheckoutPaymentDetails') == vcPaymentDetailsData
        }
    }

    @Test
    def 'prepareKlarnaCheckoutData: should not setup Klarna data if such payment method is not available'()
    {
        when:
        controller.prepareKlarnaCheckoutData(model)

        then:
        1 * paymentModeFacade.
                isPaymentModeSupported(ALTERNATIVE_PAYMENT, AlternativePaymentMethod.KLI) >> false

        with(model.asMap()) {
            get('klarnaEnabled') == null
            get('klarnaSDKUrl') == null
        }
    }

    @Test
    def 'prepareKlarnaCheckoutData: should setup Klarna data if such payment method is available'()
    {
        when:
        controller.prepareKlarnaCheckoutData(model)

        then:
        1 * paymentModeFacade.
                isPaymentModeSupported(ALTERNATIVE_PAYMENT, AlternativePaymentMethod.KLI) >> true

        with(model.asMap()) {
            get('klarnaEnabled') == true
            get('klarnaSDKUrl') == 'http:klarna'
        }
    }

    @Test
    def 'Should prepare Google Pay data'()
    {
        given:
        paymentModeFacade.isPaymentModeSupported(ALTERNATIVE_PAYMENT, GGP) >> true
        def merchant = Mock(Merchant)
        merchant.id >> 'merchId'

        when:
        controller.prepareGooglePayCheckoutData(model)

        then:
        1 * merchantService.getCurrentMerchant(GOOGLE_PAY) >> merchant
        1 * site.name >> 'siteName'
        with(model.asMap()) {
            get('googlePayEnabled') == true
            get('googlePayEnvironment') == 'TEST'
            get('googlePayGatewayMerchantId') == 'merchId'
            get('googlePayMerchantId') == '1234'
            get('googlePayMerchantName') == 'siteName'
        }
    }

    @Test
    def 'Should not prepare Google Pay data'()
    {
        given:
        paymentModeFacade.isPaymentModeSupported(ALTERNATIVE_PAYMENT, GGP) >> false

        when:
        controller.prepareGooglePayCheckoutData(model)

        then:
        0 * merchantService.getCurrentMerchant(GOOGLE_PAY)
        0 * site.name
        with(model.asMap()) {
            get('googlePayEnabled') == false
            get('googlePayEnvironment') == null
            get('googlePayGatewayMerchantId') == null
            get('googlePayMerchantId') == null
            get('googlePayMerchantName') == null
        }
    }
}
