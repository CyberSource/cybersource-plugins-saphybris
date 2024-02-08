package isv.sap.payment.service.executor.request.populator.processinglevel.l2

import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.product.ProductModel
import org.junit.Test

import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec

import static isv.cjl.payment.enums.CardType.VISA
import static isv.sap.payment.constants.IsvPaymentConstants.ProductCodeProperties.DEFAULT_PRODUCT_CODE
import static java.math.BigDecimal.ONE
import static java.math.BigDecimal.ZERO

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
        fields.otherTaxNationalTaxAmount == ZERO
        fields.otherTaxNationalTaxIndicator == '1'
        fields.invoiceHeaderSummaryCommodityCode == 'SUMM'
    }

    @Test
    def 'should populate entry data'()
    {
        given:
        def entry = Mock([useObjenesis: false], AbstractOrderEntryModel)
        def product = Mock([useObjenesis: false], ProductModel)
        def order = order('code')

        product.code >> 'code'
        product.name >> 'name'

        entry.order >> order
        entry.product >> product
        entry.quantity >> 10
        entry.basePrice >> 200D
        entry.totalPrice >> 2000D
        entry.entryNumber >> 0

        populator.configurationService = configurationService

        and:
        def target = target()

        when:
        populator.populateEntryData(entry, target)

        and:
        def fields = target.request().requestFields

        then:
        1 * configurationService.getRequiredString(DEFAULT_PRODUCT_CODE) >> 'item product code'

        fields.'0:itemTaxAmount' == ZERO
        fields.'0:itemId' == ZERO
        fields.'0:itemQuantity' == 10
        fields.'0:itemProductCode' == 'item product code'
        fields.'0:itemProductName' == 'name'
        fields.'0:itemTotalAmount' == 2000G
        fields.'0:itemUnitPrice' == 200G
        fields.'0:itemTaxRate' == ZERO
        fields.'0:itemAlternateTaxRate' == ZERO
        fields.'0:itemCommodityCode' == '45648997'
    }

    @Test
    def 'should populate shipment item'()
    {
        given:
        def order = order('code')

        order.deliveryCost >> 5.5

        populator.configurationService = configurationService

        and:
        def target = target()

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
        fields.'1:itemTotalAmount' == 5.5
        fields.'1:itemUnitPrice' == 5.5
        fields.'1:itemTaxRate' == ZERO
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
