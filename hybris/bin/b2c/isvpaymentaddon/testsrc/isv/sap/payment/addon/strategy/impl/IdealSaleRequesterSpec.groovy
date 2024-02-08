package isv.sap.payment.addon.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@UnitTest
class IdealSaleRequesterSpec extends AbstractSaleRequesterSpec
{
    @Override
    AbstractSaleRequester createRequester()
    {
        new IdealSaleRequester()
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
        [false, false, false, false, true, false]
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()
    {
        AlternativePaymentMethod.SOF
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()
    {
        AlternativePaymentMethod.IDL
    }

    def assertRequest(request, cart, type)
    {
        super.assertRequest(request, cart, type)

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
        saleRequester.initiateSale(cart, alternativePaymentTypeWhichIsSupported, 'test-merchant', [paymentOptionId: 'ID'])

        then:
        1 * paymentServiceExecutor.execute {
            assertWithOptionalParam(it, cart, alternativePaymentTypeWhichIsSupported.name())
        }
    }

    def assertWithOptionalParam(request, cart, type)
    {
        assertRequest(request, cart, type)
        assert request.requestParams['apSaleServicePaymentOptionID'] == 'ID'

        true
    }
}
