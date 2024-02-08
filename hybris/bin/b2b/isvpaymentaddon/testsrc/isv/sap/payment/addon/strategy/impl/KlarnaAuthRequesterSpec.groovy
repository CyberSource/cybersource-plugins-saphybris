package isv.sap.payment.addon.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@UnitTest
class KlarnaAuthRequesterSpec extends AbstractSaleRequesterSpec
{
    @Override
    AbstractSaleRequester createRequester()
    {
        new KlarnaAuthRequester()
    }

    @Override
    List getSupportedExceptedTypesExpectedResults()
    {
        [false, false, false, false, false, true, false]
    }

    @Override
    List getSupportedExceptedTypesInputs()
    {
        [AlternativePaymentMethod.APY,
         AlternativePaymentMethod.AYM,
         AlternativePaymentMethod.SOF,
         AlternativePaymentMethod.MCH,
         AlternativePaymentMethod.IDL,
         AlternativePaymentMethod.KLI,
         AlternativePaymentMethod.WQR]
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()
    {
        AlternativePaymentMethod.IDL
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()
    {
        AlternativePaymentMethod.KLI
    }

    @Override
    def prepareOptionalParamsForInitSale()
    {
        [(IsvPaymentAddonConstants.AlternativePayments.KLARNA_AUTH_TOKEN): 'xxx000yyy']
    }

    @Override
    void setUpAdditionalData()
    {
        siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL) >> 'thrid.com?type='
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid.com?type=') >> 'https://thrid.com?type='

        siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL) >> 'thrid_cancel'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_cancel') >> 'https://thrid_cancel.com'

        siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL) >> 'thrid_fail'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_fail') >> 'https://thrid_fail.com'

        siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.KLARNA_LANGUAGE) >> 'en-GB'
    }

    @Override
    def assertRequest(PaymentServiceRequest request, AbstractOrderModel cart, String type)
    {
        assert request.requestParams['order'].is(cart)
        assert request.requestParams['merchantId'] == 'test-merchant'

        assert request.requestParams['apAuthServiceSuccessURL'] == 'https://thrid.com?type=KLI'
        assert request.requestParams['apAuthServiceCancelURL'] == 'https://thrid_cancel.com'
        assert request.requestParams['apAuthServiceFailureURL'] == 'https://thrid_fail.com'
        assert request.requestParams['billToLanguage'] == 'en-GB'

        assert request.requestParams['apAuthServicePreapprovalToken'] == 'xxx000yyy'

        true
    }

    @Test
    def 'initiateSale: Should make a correct call to paymentExcecutor using reconciliation ID'()
    {
        given:
        AbstractOrderModel cart = new AbstractOrderModel()
        configuration.getString(AbstractSaleRequester.MERCHANT_NAME) >> 'Merchant name'
        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL) >> 'thrid.com?type='
        configuration.getString(AlipaySaleRequester.RECONCILATION_ID) >> 'ID'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid.com?type=') >> 'https://thrid.com?type='
        setUpAdditionalData()

        when:
        saleRequester.initiateSale(cart, alternativePaymentTypeWhichIsSupported, 'test-merchant', [klarnaAuthToken: null])

        then:
        thrown(IllegalArgumentException)
    }
}
