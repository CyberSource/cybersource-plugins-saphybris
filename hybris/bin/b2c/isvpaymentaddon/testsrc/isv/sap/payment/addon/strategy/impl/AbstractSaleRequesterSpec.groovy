package isv.sap.payment.addon.strategy.impl

import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.site.BaseSiteService
import org.apache.commons.configuration.Configuration
import org.junit.Test
import spock.lang.Ignore
import spock.lang.Specification

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@Ignore
abstract class AbstractSaleRequesterSpec extends Specification
{
    AbstractSaleRequester saleRequester

    PaymentServiceExecutor paymentServiceExecutor = Mock([useObjenesis: false])

    ConfigurationService configurationService = Mock([useObjenesis: false])

    Configuration configuration = Mock([useObjenesis: false])

    BaseSiteService baseSiteService = Mock([useObjenesis: false])

    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock([useObjenesis: false])

    SiteConfigService siteConfigService = Mock([useObjenesis: false])

    BaseSiteModel site = Mock([useObjenesis: false])

    abstract AbstractSaleRequester createRequester()

    def setup()
    {
        saleRequester = createRequester()

        saleRequester.paymentServiceExecutor = paymentServiceExecutor
        saleRequester.configurationService = configurationService
        saleRequester.baseSiteService = baseSiteService
        saleRequester.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        saleRequester.siteConfigService = siteConfigService

        configurationService.configuration >> configuration
        baseSiteService.currentBaseSite >> site
    }

    abstract List supportedExceptedTypesExpectedResults

    abstract List supportedExceptedTypesInputs

    @Test
    def 'supports: Should support expected types'()
    {
        expect:
        saleRequester.supports(paymentType) == result

        where:
        paymentType << getSupportedExceptedTypesInputs()
        result << getSupportedExceptedTypesExpectedResults()
    }

    @Test
    def 'supports: Should throw exception in case of null'()
    {
        when:
        saleRequester.supports(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Parameter paymentType can not be null'
    }

    abstract AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()

    @Test
    def 'initiateSale: Should throw exception if paymentType is not supported'()
    {
        given:
        AbstractOrderModel cart = new AbstractOrderModel()

        when:
        saleRequester.initiateSale(cart, alternativePaymentTypeWhichIsNotSupported,
                                   'xxx', [] as Map)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Given requester doesn\'t supports: ' + alternativePaymentTypeWhichIsNotSupported
    }

    abstract AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()

    @Test
    def 'initiateSale: Should make a correct call to paymentExcecutor'()
    {
        given:
        AbstractOrderModel cart = new AbstractOrderModel()
        configuration.getString(AbstractSaleRequester.MERCHANT_NAME) >> 'Merchant name'
        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL) >> 'thrid.com?type='
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid.com?type=') >> 'https://thrid.com?type='
        setUpAdditionalData()

        when:
        saleRequester.initiateSale(cart, alternativePaymentTypeWhichIsSupported, 'test-merchant', prepareOptionalParamsForInitSale())

        then:
        1 * paymentServiceExecutor.execute {
            assertRequest(it, cart, alternativePaymentTypeWhichIsSupported.name())
        }
    }

    def prepareOptionalParamsForInitSale()
    {
        [:]
    }

    def assertRequest(PaymentServiceRequest request, AbstractOrderModel cart, String type)
    {
        assert request.requestParams['order'].is(cart)
        assert request.requestParams['merchantId'] == 'test-merchant'
        assert request.requestParams['apPaymentType'] == type

        true
    }

    abstract void setUpAdditionalData()
}
