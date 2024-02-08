package isv.sap.payment.service

import com.google.common.collect.ImmutableMap
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.enumeration.EnumerationService
import de.hybris.platform.servicelayer.i18n.CommonI18NService
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.search.SearchResult
import de.hybris.platform.site.BaseSiteService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.model.Merchant
import isv.cjl.payment.model.MerchantProfile
import isv.cjl.payment.security.service.CredentialHolder
import isv.cjl.payment.security.service.CredentialHolderFactory
import isv.sap.payment.configuration.service.PaymentConfigurationService
import isv.sap.payment.enums.IsvConfigurationType
import isv.sap.payment.enums.IsvPaymentChannel
import isv.sap.payment.enums.MerchantProfileType
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvMerchantModel
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel
import isv.sap.payment.model.IsvMerchantProfileModel

import static isv.sap.payment.enums.PaymentType.CREDIT_CARD
import static isv.sap.payment.enums.PaymentType.VISA_CHECKOUT

@UnitTest
class IsvHybrisMerchantServiceSpec extends Specification
{
    def site = Mock([useObjenesis: false], BaseSiteModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def merchantModel = Mock([useObjenesis: false], IsvMerchantModel)

    def merchant = Mock([useObjenesis: false], Merchant)

    def paymentConfiguration = Mock([useObjenesis: false], IsvMerchantPaymentConfigurationModel)

    def merchantProfileSOPModel = Mock([useObjenesis: false], IsvMerchantProfileModel)

    def merchantProfileHOPModel = Mock([useObjenesis: false], IsvMerchantProfileModel)

    def merchantProfileVCOModel = Mock([useObjenesis: false], IsvMerchantProfileModel)

    def merchantProfileSOP = Mock([useObjenesis: false], MerchantProfile)

    def merchantProfileHOP = Mock([useObjenesis: false], MerchantProfile)

    def merchantProfileVCO = Mock([useObjenesis: false], MerchantProfile)

    def siteService = Mock([useObjenesis: false], BaseSiteService)

    def i18NService = Mock([useObjenesis: false], CommonI18NService)

    def paymentConfigService = Mock([useObjenesis: false], PaymentConfigurationService)

    def flexSearchService = Mock([useObjenesis: false], FlexibleSearchService)

    def credentialHolderFactory = Mock([useObjenesis: false], CredentialHolderFactory)

    def credentialHolder = new CredentialHolder(null, null, null)

    def enumService = Mock([useObjenesis: false], EnumerationService)

    def service = new IsvHybrisMerchantService()

    def setup()
    {
        service.baseSiteService = siteService
        service.commonI18NService = i18NService
        service.flexibleSearchService = flexSearchService
        service.enumerationService = enumService
        service.paymentConfigurationService = paymentConfigService
        service.credentialHolderFactory = credentialHolderFactory

        siteService.currentBaseSite >> site
        i18NService.currentCurrency >> currency

        paymentConfiguration.merchant >> merchantModel

        merchantProfileSOPModel.profileType >> MerchantProfileType.SOP
        merchantProfileHOPModel.profileType >> MerchantProfileType.HOP
        merchantProfileVCOModel.profileType >> MerchantProfileType.VCO

        merchantModel.profiles >> [merchantProfileSOPModel, merchantProfileHOPModel, merchantProfileVCOModel]

        merchantProfileSOP.profileType >> isv.cjl.payment.enums.MerchantProfileType.SOP
        merchantProfileHOP.profileType >> isv.cjl.payment.enums.MerchantProfileType.HOP
        merchantProfileVCO.profileType >> isv.cjl.payment.enums.MerchantProfileType.VCO

        merchant.profiles >> [merchantProfileSOP, merchantProfileHOP, merchantProfileVCO]

        credentialHolderFactory.getCredentialHolder() >> credentialHolder
    }

    @Test
    def 'should return merchant by given id'()
    {
        given:
        merchantModel.id >> 'merchant_1'
        merchantModel.userName >> 'merchant_1'
        merchantModel.passwordToken >> 'passwordToken'
        paymentConfigService.getConfiguration(IsvConfigurationType.MERCHANT, [id: 'merchant_1']) >> merchantModel

        def credentialHolderFactory = Stub([useObjenesis: false], CredentialHolderFactory)
        credentialHolderFactory.getCredentialHolder(_, _) >> { args ->
            Stub([constructorArgs: [null, null, null], useObjenesis: false], CredentialHolder) {
                getIdentity() >> args[0]
                getSecret() >> args[1]
            }
        }
        service.credentialHolderFactory = credentialHolderFactory

        when:
        def merchant = service.getMerchant('merchant_1')

        then:
        merchant.id == 'merchant_1'
        with(merchant.credentialHolder) {
            identity == 'merchant_1'
            secret == 'passwordToken'
        }
    }

    @Test
    def 'should return merchant profile by given string type'()
    {
        when:
        def profile = service.getMerchantProfile(merchant, 'HOP')

        then:
        profile == merchantProfileHOP
    }

    @Test
    def 'should return merchant profile by given type'()
    {
        when:
        def profile = service.getMerchantProfile(merchant, isv.cjl.payment.enums.MerchantProfileType.SOP)

        then:
        profile == merchantProfileSOP
    }

    @Test
    def 'should return current merchant by site, currency, channel, etc'()
    {
        given:
        merchantModel.id >> 'merchant_2'
        enumService.getEnumerationValue(
                PaymentType, 'CREDIT_CARD') >> CREDIT_CARD
        paymentConfigService.getConfiguration(IsvConfigurationType.MERCHANT_CONFIG, ImmutableMap.of(
                IsvMerchantPaymentConfigurationModel.SITE, siteService.currentBaseSite,
                IsvMerchantPaymentConfigurationModel.PAYMENTTYPE, CREDIT_CARD,
                IsvMerchantPaymentConfigurationModel.PAYMENTCHANNEL, IsvPaymentChannel.WEB,
                IsvMerchantPaymentConfigurationModel.CURRENCY, i18NService.currentCurrency
        )) >> paymentConfiguration

        when:
        def profile = service.getCurrentMerchant(isv.cjl.payment.enums.PaymentType.CREDIT_CARD)

        then:
        profile.id == 'merchant_2'
    }

    @Test
    def 'should return current merchant profile by given type'()
    {
        given:
        enumService.getEnumerationValue(
                PaymentType, 'CREDIT_CARD') >> CREDIT_CARD
        setupPaymentConfiguration(CREDIT_CARD)

        when:
        def profile = service.getCurrentMerchantProfile(isv.cjl.payment.enums.MerchantProfileType.SOP)

        then:
        profile.profileType == isv.cjl.payment.enums.MerchantProfileType.SOP
    }

    def setupPaymentConfiguration(PaymentType paymentType)
    {
        paymentConfigService.getConfiguration(IsvConfigurationType.MERCHANT_CONFIG, ImmutableMap.of(
                IsvMerchantPaymentConfigurationModel.SITE, siteService.currentBaseSite,
                IsvMerchantPaymentConfigurationModel.PAYMENTTYPE, paymentType,
                IsvMerchantPaymentConfigurationModel.PAYMENTCHANNEL, IsvPaymentChannel.WEB,
                IsvMerchantPaymentConfigurationModel.CURRENCY, i18NService.currentCurrency
        )) >> paymentConfiguration
    }

    @Test
    def 'service should return all merchants'()
    {
        def searchResult = Mock([useObjenesis: false], SearchResult)
        searchResult.result >> []

        when:
        service.allMerchants

        then:
        1 * flexSearchService.search('SELECT {' + IsvMerchantModel.PK + '} FROM {' + IsvMerchantModel._TYPECODE + '}') >> searchResult
    }

    @Test
    def 'getMerchantProfileForPaymentType: should return requested profile'()
    {
        given:
        enumService.getEnumerationValue(
                PaymentType, 'VISA_CHECKOUT') >> VISA_CHECKOUT
        setupPaymentConfiguration(VISA_CHECKOUT)

        when:
        def profile = service.getMerchantProfileForPaymentType(isv.cjl.payment.enums.PaymentType.VISA_CHECKOUT, isv.cjl.payment.enums.MerchantProfileType.VCO)

        then:
        profile.profileType == isv.cjl.payment.enums.MerchantProfileType.VCO
    }
}
