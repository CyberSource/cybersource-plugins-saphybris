package isv.sap.payment.addon.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@UnitTest
class BancontactSaleRequesterSpec extends AbstractSaleRequesterSpec
{
    @Override
    AbstractSaleRequester createRequester()
    {
        new BancontactSaleRequester()
    }

    @Override
    List getSupportedExceptedTypesInputs()
    {
        [AlternativePaymentMethod.APY,
         AlternativePaymentMethod.AYM,
         AlternativePaymentMethod.SOF,
         AlternativePaymentMethod.MCH,
         AlternativePaymentMethod.IDL,
         AlternativePaymentMethod.WQR]
    }

    @Override
    List getSupportedExceptedTypesExpectedResults()
    {
        [false, false, false, true, false, false]
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()
    {
        AlternativePaymentMethod.SOF
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()
    {
        AlternativePaymentMethod.MCH
    }

    def assertRequest(request, cart, type)
    {
        Object.assertRequest(request, cart, type)

        assert request.requestParams['apSaleServiceSuccessURL'] == 'https://thrid.com?type=' + type
        assert request.requestParams['apSaleServiceCancelURL'] == 'https://thrid_cancel.com'
        assert request.requestParams['apSaleServiceFailureURL'] == 'https://thrid_fail.com'

        true
    }

    @Override
    void setUpAdditionalData()
    {
        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL) >> 'thrid_cancel'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_cancel') >> 'https://thrid_cancel.com'

        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL) >> 'thrid_fail'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_fail') >> 'https://thrid_fail.com'
    }
}
