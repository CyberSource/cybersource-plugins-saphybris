package isv.sap.payment.service.executor.request.populator.processinglevel.l3

import org.junit.Test

import isv.cjl.payment.enums.ProcessingLevel
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec

import static isv.cjl.payment.enums.CardType.VISA
import static java.math.BigDecimal.ONE
import static java.math.BigDecimal.ZERO
import static java.math.BigDecimal.valueOf

class OmniPayVisaPopulatorSpec extends AbstractPopulatorSpec
{
    def populator = new OmniPayVisaPopulator()

    @Test
    def 'should populate order data'()
    {
        given:
        def order = order('code')

        and:
        def target = target()

        when:
        populator.populateOrderData(order, target)

        and:
        def fields = target.request().requestFields

        then:
        fields.invoiceHeaderUserPO == 'code'
        fields.invoiceHeaderSupplierOrderReference == '456456346'
        fields.invoiceHeaderMerchantVATRegistrationNumber == 'US-123445555'
        fields.purchaseTotalsTaxAmount == ZERO
        fields.otherTaxNationalTaxAmount == ZERO
        fields.otherTaxNationalTaxIndicator == '1'
        fields.purchaseTotalsFreightAmount == ZERO
        fields.otherTaxVatTaxRate == valueOf(0.001)
        fields.invoiceHeaderSummaryCommodityCode == 'SUMM'

        populator.level == ProcessingLevel.L3
    }

    @Test
    def 'should populate entry data'()
    {
        given:
        def target = target()
        def order = order('code')
        def entry = entry(2, 'code', 'name', 35)

        entry.order >> order
        populator.configurationService = configurationService

        when:
        populator.populateEntryData(entry, target)

        and:
        def fields = target.request().requestFields

        then:
        1 * configurationService.getRequiredString(IsvPaymentConstants.ProductCodeProperties.DEFAULT_PRODUCT_CODE) >> 'item product code'

        fields.'0:itemTaxAmount' == ZERO
        fields.'0:itemId' == ZERO
        fields.'0:itemQuantity' == 2
        fields.'0:itemProductCode' == 'item product code'
        fields.'0:itemProductName' == 'name'
        fields.'0:itemTotalAmount' == 70G
        fields.'0:itemUnitPrice' == 35G
        fields.'0:itemAlternateTaxRate' == ZERO
        fields.'0:itemCommodityCode' == '45648997'
    }

    @Test
    def 'should populate shipping item'()
    {
        given:
        def target = target()
        def order = order('code')

        order.deliveryCost >> 2.2
        populator.configurationService = configurationService

        when:
        populator.populateShippingItem(order, target)

        and:
        def fields = target.request().requestFields

        then:
        1 * configurationService.getRequiredString(IsvPaymentConstants.ProductCodeProperties.SHIPPING_PRODUCT_CODE) >> 'shipping product code'

        fields.'1:itemTaxAmount' == ZERO
        fields.'1:itemId' == ONE
        fields.'1:itemQuantity' == 1
        fields.'1:itemProductCode' == 'shipping product code'
        fields.'1:itemProductName' == 'SHIPPING'
        fields.'1:itemTotalAmount' == 2.2
        fields.'1:itemUnitPrice' == 2.2
        fields.'1:itemAlternateTaxRate' == ZERO
        fields.'1:itemCommodityCode' == '45648997'
    }

    @Test
    def 'should return Visa card type'()
    {
        expect:
        new OmniPayVisaPopulator().cardType == VISA
    }
}
