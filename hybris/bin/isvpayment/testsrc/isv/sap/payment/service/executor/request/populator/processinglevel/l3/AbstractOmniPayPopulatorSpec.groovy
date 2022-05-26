package isv.sap.payment.service.executor.request.populator.processinglevel.l3

import org.apache.commons.lang3.StringUtils
import org.junit.Test
import spock.lang.Unroll

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.constants.IsvPaymentConstants
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulatorSpec
import isv.sap.payment.service.executor.request.populator.processinglevel.ProcessingLevelOperation

import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT
import static isv.cjl.payment.enums.PaymentProcessor.OMNIPAY_DIRECT
import static isv.cjl.payment.enums.ProcessingLevel.L3
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
        fields.purchaseTotalsDiscountAmount == ZERO

        populator.level == L3
    }

    @Test
    def 'should populate entry data'()
    {
        given:
        def target = target()
        populator.configurationService = configurationService

        def entry = entry(2, 'code', name, 50)
        def order = order('code')
        entry.order >> order

        if (configuredMaxSize)
        {
            populator.setMaxProductNameSize(maxProductNameSize)
        }

        when:
        populator.populateEntryData(entry, target)

        then:
        def fields = target.request().requestFields

        1 * configurationService.getRequiredString(IsvPaymentConstants.ProductCodeProperties.DEFAULT_PRODUCT_CODE) >> 'item product code'

        taxAmount == fields.'0:itemTaxAmount'
        itemId == fields.'0:itemId'
        quantity == fields.'0:itemQuantity'
        productCode == fields.'0:itemProductCode'
        productName == fields.'0:itemProductName'
        totalAmount == fields.'0:itemTotalAmount'
        unitPrice == fields.'0:itemUnitPrice'
        taxRate == fields.'0:itemTaxRate'
        discount == fields.'0:itemDiscountAmount'
        fields.'0:itemProductSKU' == 'code'

        where:
        configuredMaxSize | maxProductNameSize | name            | taxAmount | itemId | quantity | productCode         | productName     | totalAmount | unitPrice | taxRate | discount
        true              | 3                  | 'name'          | ZERO      | ZERO   | 2        | 'item product code' | 'nam'           | 100G        | 50G       | ZERO    | ZERO
        true              | 4                  | 'name'          | ZERO      | ZERO   | 2        | 'item product code' | 'name'          | 100G        | 50G       | ZERO    | ZERO
        false             | _                  | 'aVeryLongName' | ZERO      | ZERO   | 2        | 'item product code' | 'aVeryLongName' | 100G        | 50G       | ZERO    | ZERO
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
        fields.'1:itemTaxRate' == ZERO
        fields.'1:itemProductSKU' == 'SHIPPING'
        fields.'1:itemDiscountAmount' == ZERO
    }

    @Test
    def 'should return 3rd processing level'()
    {
        expect:
        populator.level == L3
    }

    @Test
    def 'should return OmniPay processor'()
    {
        expect:
        populator.paymentProcessor == OMNIPAY_DIRECT
    }

    @Test
    def 'should populate order data for target != capture'()
    {
        given:
        def order = order('code')

        def target = new RequestFactory(validators: []).request()
                .addParam(MERCHANT_ID, StringUtils.EMPTY)
                .addParam(MERCHANT_REFERENCE_CODE, StringUtils.EMPTY)
                .addParam(PURCHASE_TOTALS_CURRENCY, StringUtils.EMPTY)
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, ZERO)

        when:
        populator.populateOrderData(order, target)

        and:
        def fields = target.request().requestFields

        then:
        fields.invoiceHeaderUserPO == 'code'
        fields.invoiceHeaderSupplierOrderReference == '456456346'
        fields.invoiceHeaderMerchantVATRegistrationNumber == 'US-123445555'
        fields.purchaseTotalsTaxAmount == ZERO
        fields.purchaseTotalsDiscountAmount == ZERO
        fields.ccCaptureServicePurchasingLevel == null
    }

    @Test
    @Unroll
    def 'should populate purchasing level data for target'()
    {
        given:
        def target = Mock(PaymentTransaction)

        when:
        populator.populatePurchasingLevelData(levelOperation, target)

        then:
        1 * target.addParam(paramName, '3')

        where:
        levelOperation                   | paramName
        ProcessingLevelOperation.CAPTURE | 'ccCaptureServicePurchasingLevel'
        ProcessingLevelOperation.CREDIT  | 'ccCreditServicePurchasingLevel'
    }
}
