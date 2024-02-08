package isv.sap.payment.addon.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants

@UnitTest
class AlipaySaleRequesterSpec extends AbstractSaleRequesterSpec
{
    @Override
    AbstractSaleRequester createRequester()
    {
        new AlipaySaleRequester()
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
        [true, true, false, false, false, false]
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsNotSupported()
    {
        AlternativePaymentMethod.MCH
    }

    @Override
    AlternativePaymentMethod getAlternativePaymentTypeWhichIsSupported()
    {
        AlternativePaymentMethod.AYM
    }

    def assertRequest(request, cart, type)
    {
        super.assertRequest(request, cart, type)

        assert request.requestParams['productName'] == 'Merchant name'
        assert request.requestParams['returnUrl'] == 'https://thrid.com?type=' + type

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
        saleRequester.initiateSale(cart, alternativePaymentTypeWhichIsSupported, 'test-merchant', prepareOptionalParamsForInitSale())

        then:
        1 * paymentServiceExecutor.execute {
            assertReconciliationIDRequest(it, cart, alternativePaymentTypeWhichIsSupported.name())
        }
    }

    def assertReconciliationIDRequest(request, cart, type)
    {
        assertRequest(request, cart, type)
        assert request.requestParams['reconciliationID'] == 'ID'

        true
    }

    @Override
    void setUpAdditionalData()
    {
        // EMPTY
    }
}
