package isv.sap.payment.service.executor.request.populator.processinglevel

import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.product.ProductModel
import org.apache.commons.configuration.Configuration
import org.apache.commons.lang3.StringUtils
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.configuration.service.ConfigurationService
import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.PaymentProcessor
import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.ProcessingLevel
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.executor.request.converter.processinglevel.param.ProcessingLevelParam
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_AUTH_REQUEST_ID
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_RUN
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT
import static java.math.BigDecimal.ZERO

class AbstractPopulatorSpec extends Specification
{
    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    def configuration = Mock([useObjenesis: false], Configuration)

    def populator = Spy([useObjenesis: false], AbstractPopulator) {
        configurationService.configuration >> configuration

        _ * getCardType() >> CardType.VISA
        _ * getLevel() >> ProcessingLevel.L2
        _ * getPaymentProcessor() >> PaymentProcessor.OMNIPAY_DIRECT
    }

    @Test
    def 'should detect supported processing level'()
    {
        given:
        def param = new ProcessingLevelParam(level, processor, card)

        when:
        def supported = populator.supports(param)

        then:
        isProcessingLevelSupported == supported

        where:
        isProcessingLevelSupported | level              | processor                                    | card
        true                       | ProcessingLevel.L2 | PaymentProcessor.OMNIPAY_DIRECT              | CardType.VISA
        false                      | ProcessingLevel.L2 | PaymentProcessor.CHASE_PAYMENTECH_SOLUTIONS  | CardType.VISA
        false                      | ProcessingLevel.L2 | PaymentProcessor.CYBERSOURCE_THROUGH_VISANET | CardType.DINERS
        false                      | ProcessingLevel.L2 | PaymentProcessor.GPN                         | CardType.MASTERCARD_EUROCARD
        false                      | ProcessingLevel.L2 | PaymentProcessor.CYBERSOURCE_THROUGH_VISANET | CardType.MAESTRO
        false                      | ProcessingLevel.L3 | PaymentProcessor.CYBERSOURCE_THROUGH_VISANET | CardType.MAESTRO
        false                      | ProcessingLevel.L3 | PaymentProcessor.GPN                         | CardType.VISA
        false                      | ProcessingLevel.L3 | PaymentProcessor.CYBERSOURCE_THROUGH_VISANET | CardType.DINERS
        false                      | ProcessingLevel.L3 | PaymentProcessor.LITTLE                      | CardType.DINERS
        false                      | ProcessingLevel.L2 | PaymentProcessor.CYBERSOURCE_THROUGH_VISANET | CardType.DINERS
    }

    @Test
    def 'should shorten provided string value'()
    {
        when:
        def actualValue = populator.shorten(value, maxSize)

        then:
        actualValue == shortValue

        where:
        value  | maxSize | shortValue
        '123'  | 3       | '123'
        '1234' | 2       | '12'
        '1234' | 5       | '1234'
    }

    @Test
    @Unroll
    def 'Should populate into target successfully when transaction type is capture or refund'()
    {
        given:
        def source = PaymentServiceRequest.create()
        source.paymentTransactionType = transactionType

        def entry = Mock([useObjenesis: false], AbstractOrderEntryModel)
        def order = Mock([useObjenesis: false], AbstractOrderModel)

        order.entries >> [entry]
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        def target = new PaymentTransaction()

        when:
        populator.populate(source, target)

        then:
        1 * populator.populateOrderData(order, target) >> null
        1 * populator.populateEntryData(entry, target) >> null
        1 * populator.populateShippingItem(order, target) >> null

        where:
        transactionType                          | _
        PaymentTransactionType.CAPTURE           | _
        PaymentTransactionType.REFUND            | _
        PaymentTransactionType.REFUND_FOLLOW_ON  | _
        PaymentTransactionType.REFUND_STANDALONE | _
    }

    @Test
    def 'Should not populate into target when transaction type is not supported'()
    {
        given:
        def source = PaymentServiceRequest.create()
        source.paymentTransactionType = PaymentTransactionType.CANCEL
        def target = new PaymentTransaction()

        when:
        populator.populate(source, target)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message =~ 'Processing level data cannot be applied for service request type'
        0 * populator.populateOrderData(_, _)
        0 * populator.populateEntryData(_, _)
        0 * populator.populateShippingItem(_, _)
    }

    @Test
    def 'Should not populate into when exception occurs'()
    {
        given:
        def source = PaymentServiceRequest.create()
        source.paymentTransactionType = PaymentTransactionType.CAPTURE

        def order = Mock([useObjenesis: false], AbstractOrderModel)
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        def target = new PaymentTransaction()

        populator.populateOrderData(order, target) >> { throw new IllegalStateException() }

        when:
        populator.populate(source, target)

        then:
        thrown IllegalStateException
    }

    protected order(def code)
    {
        def order = Mock([useObjenesis: false], AbstractOrderModel)
        def currency = Mock([useObjenesis: false], CurrencyModel)

        currency.digits >> 2
        order.entries >> [entry(2, 'productCode', 'productName', 100D)]
        order.code >> code
        order.currency >> currency

        order
    }

    protected entry(def qty, def code, def name, def price)
    {
        def entry = Mock([useObjenesis: false], AbstractOrderEntryModel)
        def product = Mock([useObjenesis: false], ProductModel)

        product.code >> code
        product.name >> name

        entry.product >> product
        entry.quantity >> qty
        entry.basePrice >> price
        entry.totalPrice >> price * qty
        entry.entryNumber >> 0

        entry
    }

    protected target()
    {
        new RequestFactory(validators: []).request()
                .addParam(MERCHANT_ID, StringUtils.EMPTY)
                .addParam(MERCHANT_REFERENCE_CODE, StringUtils.EMPTY)
                .addParam(CC_CAPTURE_SERVICE_RUN, true)
                .addParam(CC_CAPTURE_SERVICE_AUTH_REQUEST_ID, StringUtils.EMPTY)
                .addParam(PURCHASE_TOTALS_CURRENCY, StringUtils.EMPTY)
                .addParam(CC_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN, StringUtils.EMPTY)
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, ZERO)
    }
}
