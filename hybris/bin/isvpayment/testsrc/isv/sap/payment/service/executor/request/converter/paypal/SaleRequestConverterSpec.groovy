package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.SALE

@UnitTest
class SaleRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def orderEntry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    def product = Mock([useObjenesis: false], ProductModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def converter = new SaleRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(SALE) >> paymentTransaction

        populateDeliveryAddress()
        populateOrderEntry()

        country.isocode >> 'GB'
        currency.isocode >> 'USD'
        currency.digits >> 2
        cart.guid >> 'guid'
        cart.deliveryAddress >> deliveryAddress
        cart.currency >> currency
        cart.totalPrice >> 25
        cart.entries >> [orderEntry]
        cart.deliveryCost >> 15

        transactionEntry.requestId >> 'ORDER_REQUEST_ID'

        source.addParam('order', cart)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('purchaseTotalsDiscountAmount', BigDecimal.TEN)
        source.addParam('purchaseTotalsTaxAmount', BigDecimal.ONE)
        source.addParam('transaction', transactionEntry)
    }

    @Test
    def 'should convert source to create session request'()
    {
        when:
        def target = converter.convert(source)

        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'PPL'
        requestFields.apSaleServiceRun == true
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 25
        requestFields.apSaleServiceOrderRequestID == 'ORDER_REQUEST_ID'

        assertShipToInfo(requestFields)
        assertItems(requestFields)
    }

    def populateDeliveryAddress()
    {
        deliveryAddress.country >> country
        deliveryAddress.town >> 'London'
        deliveryAddress.firstname >> 'Jack'
        deliveryAddress.lastname >> 'Taylor'
        deliveryAddress.postalcode >> '54321'
        deliveryAddress.line1 >> 'ship to address line 1'
    }

    void assertShipToInfo(Map<String, Object> requestFields)
    {
        assert requestFields.shipToCity == 'London'
        assert requestFields.shipToCountry == 'GB'
        assert requestFields.shipToFirstName == 'Jack'
        assert requestFields.shipToLastName == 'Taylor'
        assert requestFields.shipToPostalCode == '54321'
        assert requestFields.shipToStreet1 == 'ship to address line 1'
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
