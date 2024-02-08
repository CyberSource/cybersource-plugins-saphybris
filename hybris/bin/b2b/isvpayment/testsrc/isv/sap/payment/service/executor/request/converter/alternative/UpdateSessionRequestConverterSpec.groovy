package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.UPDATE_SESSION

@UnitTest
class UpdateSessionRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def orderEntry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    def product = Mock([useObjenesis: false], ProductModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def usCountry = Mock([useObjenesis: false], CountryModel)

    def converter = new UpdateSessionRequestConverter()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(UPDATE_SESSION) >> new PaymentTransaction()

        populateBillingAddress()
        populateDeliveryAddress()
        populateOrderEntry()

        paymentInfo.billingAddress >> billingAddress

        transaction.requestId >> 'request_123'

        currency.isocode >> 'USD'
        currency.digits >> 2
        cart.guid >> 'guid'
        cart.paymentInfo >> paymentInfo
        cart.deliveryAddress >> deliveryAddress
        cart.currency >> currency
        cart.totalPrice >> 25
        cart.entries >> [orderEntry]
        cart.deliveryCost >> 15

        source.addParam('order', cart)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('apSessionsServiceCancelURL', 'cancelURL')
        source.addParam('apSessionsServiceFailureURL', 'failureURL')
        source.addParam('apSessionsServiceSuccessURL', 'successURL')
        source.addParam('billToLanguage', 'en-US')
        source.addParam('purchaseTotalsDiscountAmount', BigDecimal.TEN)
        source.addParam('purchaseTotalsTaxAmount', BigDecimal.ONE)
    }

    @Test
    def 'should convert source to create session request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'KLI'
        requestFields.apSessionsServiceRun == true
        requestFields.apSessionsServiceRequestID == 'request_123'
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.apSessionsServiceCancelURL == 'cancelURL'
        requestFields.apSessionsServiceFailureURL == 'failureURL'
        requestFields.apSessionsServiceSuccessURL == 'successURL'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 25
        requestFields.purchaseTotalsDiscountAmount == 10
        requestFields.purchaseTotalsTaxAmount == 1

        assertBillToInfo(requestFields)
        assertShipToInfo(requestFields)
        assertItems(requestFields)
    }

    def populateDeliveryAddress()
    {
        country.isocode >> 'GB'
        deliveryAddress.country >> country
        deliveryAddress.town >> 'London'
        deliveryAddress.email >> 'shipto@mail.com'
        deliveryAddress.firstname >> 'Jack'
        deliveryAddress.lastname >> 'Taylor'
        deliveryAddress.postalcode >> '54321'
        deliveryAddress.line1 >> 'ship to address line 1'
    }

    def populateBillingAddress()
    {
        usCountry.isocode >> 'US'
        billingAddress.country >> usCountry
        billingAddress.town >> 'New York'
        billingAddress.email >> 'billto@mail.com'
        billingAddress.firstname >> 'John'
        billingAddress.lastname >> 'Doe'
        billingAddress.postalcode >> '12345'
        billingAddress.line1 >> 'bill to address line 1'
    }

    void assertBillToInfo(Map<String, Object> requestFields)
    {
        assert requestFields.billToCity == 'New York'
        assert requestFields.billToCountry == 'US'
        assert requestFields.billToEmail == 'billto@mail.com'
        assert requestFields.billToFirstName == 'John'
        assert requestFields.billToLastName == 'Doe'
        assert requestFields.billToLanguage == 'en-US'
        assert requestFields.billToPostalCode == '12345'
        assert requestFields.billToStreet1 == 'bill to address line 1'
    }

    void assertShipToInfo(Map<String, Object> requestFields)
    {
        assert requestFields.shipToCity == 'London'
        assert requestFields.shipToCountry == 'GB'
        assert requestFields.shipToEmail == 'billto@mail.com'
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
