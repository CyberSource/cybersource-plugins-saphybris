package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.product.ProductModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CREATE_SESSION

@UnitTest
class CreateSessionRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def orderEntry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    def product = Mock([useObjenesis: false], ProductModel)

    def converter = new CreateSessionRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CREATE_SESSION) >> paymentTransaction

        populateOrderEntry()

        def currency = Mock(CurrencyModel)
        currency.isocode >> 'USD'
        currency.digits >> 2

        cart.guid >> 'guid'
        cart.currency >> currency
        cart.totalPrice >> 25
        cart.entries >> [orderEntry]
        cart.deliveryCost >> 15
        cart.totalDiscounts >> 10

        source.addParam('order', cart)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('apSessionsServiceCancelURL', 'cancelURL')
        source.addParam('apSessionsServiceSuccessURL', 'successURL')
    }

    @Test
    def 'should convert source to create session request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'PPL'
        requestFields.apSessionsServiceRun == true
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.apSessionsServiceCancelURL == 'cancelURL'
        requestFields.apSessionsServiceSuccessURL == 'successURL'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsDiscountAmount == 10
        requestFields.purchaseTotalsGrandTotalAmount == 25

        assertItems(requestFields)
    }

    void assertItems(Map<String, Object> requestFields)
    {
        assert requestFields['0:itemId'] == 0
        assert requestFields['0:itemProductName'] == 'product name'
        assert requestFields['0:itemQuantity'] == 3
        assert requestFields['0:itemUnitPrice'] == 3.33
        assert requestFields['0:itemTaxAmount'] == 0
        assert requestFields['0:itemTotalAmount'] == 10

        assert requestFields['1:itemId'] == 1
        assert requestFields['1:itemProductName'] == 'SHIPPING'
        assert requestFields['1:itemQuantity'] == 1
        assert requestFields['1:itemUnitPrice'] == 15
        assert requestFields['1:itemTaxAmount'] == 0
        assert requestFields['1:itemTotalAmount'] == 15
    }

    private void populateOrderEntry()
    {
        product.name >> 'product name'
        orderEntry.product >> product
        orderEntry.quantity >> 3
        orderEntry.totalPrice >> 10
        orderEntry.order >> cart
    }
}
