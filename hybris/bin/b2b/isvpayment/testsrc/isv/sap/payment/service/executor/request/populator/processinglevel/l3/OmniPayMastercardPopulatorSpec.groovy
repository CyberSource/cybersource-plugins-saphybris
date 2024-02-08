package isv.sap.payment.service.executor.request.populator.processinglevel.l3

import org.junit.Test

import isv.cjl.payment.enums.ProcessingLevel
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec

import static java.math.BigDecimal.ZERO

class OmniPayMastercardPopulatorSpec extends AbstractPopulatorSpec
{
    @Test
    def 'should populate order data'()
    {
        given:
        def order = order('code')

        and:
        def target = target()

        when:
        def populator = new OmniPayMastercardPopulator()
        populator.populateOrderData(order, target)

        and:
        def fields = target.request().requestFields

        then:
        fields.invoiceHeaderUserPO == 'code'
        fields.invoiceHeaderSupplierOrderReference == '456456346'
        fields.invoiceHeaderMerchantVATRegistrationNumber == 'US-123445555'
        fields.purchaseTotalsTaxAmount == ZERO

        populator.level == ProcessingLevel.L3
    }

    @Test
    def 'should return Mastercard card type'()
    {
        expect:
        new OmniPayMastercardPopulator().cardType == isv.cjl.payment.enums.CardType.MASTERCARD_EUROCARD
    }
}
