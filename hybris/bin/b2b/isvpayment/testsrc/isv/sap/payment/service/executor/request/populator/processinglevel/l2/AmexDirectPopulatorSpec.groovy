package isv.sap.payment.service.executor.request.populator.processinglevel.l2

import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test

import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec

import static java.math.BigDecimal.ONE
import static java.math.BigDecimal.ZERO

class AmexDirectPopulatorSpec extends AbstractPopulatorSpec
{
    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def amexPopulator = new AmexDirectPopulator()

    def entry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    @Test
    def 'should populate order data'()
    {
        given:
        def target = target()
        def order = order('code')
        order.deliveryAddress >> deliveryAddress
        deliveryAddress.postalcode >> '12345'

        when:
        amexPopulator.populateOrderData(order, target)
        def fields = target.request().requestFields

        then:
        fields.shipToPostalCode == '12345'
        fields.invoiceHeaderAmexDataTAA1 == 'CPS item 1'
        fields.invoiceHeaderAmexDataTAA2 == 'CPS item 2'
        fields.invoiceHeaderAmexDataTAA3 == 'CPS item 3'
        fields.invoiceHeaderAmexDataTAA4 == 'CPS item 4'
    }

    @Test
    def 'should populate entry data'()
    {
        given:
        def target = target()
        def order = order('code')

        entry.totalPrice >> 10
        entry.quantity >> 3
        entry.entryNumber >> 0
        entry.order >> order

        when:
        amexPopulator.populateEntryData(entry, target)
        def fields = target.request().requestFields

        then:
        fields.'0:itemId' == ZERO
        fields.'0:itemQuantity' == 3
        fields.'0:itemTaxAmount' == ZERO
        fields.'0:itemUnitPrice' == 3.33
    }

    @Test
    def 'should populate shipping item'()
    {
        given:
        def target = target()
        def order = order('code')
        order.deliveryCost >> 5.5

        when:
        amexPopulator.populateShippingItem(order, target)
        def fields = target.request().requestFields

        then:
        fields.'1:itemId' == ONE
        fields.'1:itemQuantity' == 1
        fields.'1:itemTaxAmount' == ZERO
        fields.'1:itemUnitPrice' == 5.5
    }

    @Test
    def 'should return AMEX card type'()
    {
        expect:
        amexPopulator.cardType == isv.cjl.payment.enums.CardType.AMEX
    }

    @Test
    def 'should return L2 processing level'()
    {
        expect:
        amexPopulator.level == isv.cjl.payment.enums.ProcessingLevel.L2
    }

    @Test
    def 'should return American Express Direct payment processor'()
    {
        expect:
        amexPopulator.paymentProcessor == isv.cjl.payment.enums.PaymentProcessor.AMERICAN_EXPRESS_DIRECT
    }
}
