package isv.sap.payment.addon.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@UnitTest
class SofortSaleRequesterSpec extends AbstractSaleRequesterSpec
{
    @Override
    AbstractSaleRequester createRequester()
    {
        new SofortSaleRequester()
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
        [false, false, true, false, false, false]
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()
    {
        AlternativePaymentMethod.MCH
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()
    {
        AlternativePaymentMethod.SOF
    }

    def assertRequest(request, cart, type)
    {
        super.assertRequest(request, cart, type)

        assert request.requestParams['apSaleServiceSuccessURL'] == 'https://thrid.com?type=' + type
        assert request.requestParams['apSaleServiceCancelURL'] == 'https://thrid_cancel.com'
        assert request.requestParams['apSaleServiceFailureURL'] == 'https://thrid_failure.com'

        true
    }

    @Override
    void setUpAdditionalData()
    {
        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL) >> 'thrid_cancel'
        configuration.getString(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL) >> 'thrid_failure'

        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_cancel') >> 'https://thrid_cancel.com'
        siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'thrid_failure') >> 'https://thrid_failure.com'
    }
}
