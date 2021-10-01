package isv.sap.payment.service.executor.request.populator.processinglevel.l2

import org.junit.Test

import isv.cjl.payment.enums.CardType
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec

import static isv.cjl.payment.enums.PaymentProcessor.OMNIPAY_DIRECT
import static isv.cjl.payment.enums.ProcessingLevel.L2
import static java.math.BigDecimal.ONE
import static java.math.BigDecimal.ZERO

class AbstractOmniPayPopulatorSpec extends AbstractPopulatorSpec
{
    @SuppressWarnings('BracesForClass')
    def populator = new AbstractOmniPayPopulator() {

        @Override
        protected CardType getCardType()
        {
            throw new UnsupportedOperationException('should not be invoked here.')
        }
    }

    @Test
    def 'should return payment processor'()
    {
        expect:
        populator.paymentProcessor == OMNIPAY_DIRECT
    }

    @Test
    def 'should return processing level'()
    {
        expect:
        populator.level == L2
    }

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
    }

    @Test
    def 'should populate entry data'()
    {
        given:
        def target = target()
        populator.configurationService = configurationService

        if (configuredMaxSize)
        {
            populator.setMaxProductNameSize(maxProductNameSize)
        }

        def entry = entry(3, 'code', name, 40)
        def order = order('code')
        entry.order >> order

        when:
        populator.populateEntryData(entry, target)

        then:
        def fields = target.request().requestFields

        1 * configurationService.getRequiredString(IsvPaymentConstants.ProductCodeProperties.DEFAULT_PRODUCT_CODE) >> 'item product code'

        taxAmount == fields.'0:itemTaxAmount'
        itemId == fields.'0:itemId'
        productCode == fields.'0:itemProductCode'
        productName == fields.'0:itemProductName'
        totalAmount == fields.'0:itemTotalAmount'
        unitPrice == fields.'0:itemUnitPrice'
        sku == fields.'0:itemProductSKU'

        where:
        configuredMaxSize | maxProductNameSize | name                   | taxAmount | itemId | quantity | productCode         | productName            | totalAmount | unitPrice | sku
        true              | 4                  | 'name'                 | ZERO      | ZERO   | 3        | 'item product code' | 'name'                 | 120G        | 40G       | 'code'
        true              | 5                  | 'aVeryLongProductName' | ZERO      | ZERO   | 3        | 'item product code' | 'aVery'                | 120G        | 40G       | 'code'
        false             | _                  | 'aVeryLongProductName' | ZERO      | ZERO   | 3        | 'item product code' | 'aVeryLongProductName' | 120G        | 40G       | 'code'
    }

    @Test
    def 'should populate shipping item'()
    {
        given:
        def target = target()
        populator.configurationService = configurationService

        def order = order('code')
        order.deliveryCost >> 5.5

        when:
        populator.populateShippingItem(order, target)

        then:
        def fields = target.request().requestFields

        1 * configurationService.getRequiredString(IsvPaymentConstants.ProductCodeProperties.SHIPPING_PRODUCT_CODE) >> 'shipping product code'

        fields.'1:itemTaxAmount' == ZERO
        fields.'1:itemId' == ONE
        fields.'1:itemQuantity' == 1
        fields.'1:itemProductCode' == 'shipping product code'
        fields.'1:itemProductName' == 'SHIPPING'
        fields.'1:itemTotalAmount' == 5.5
        fields.'1:itemUnitPrice' == 5.5
        fields.'1:itemProductSKU' == 'SHIPPING'
    }
}
