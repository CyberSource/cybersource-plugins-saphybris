package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.CREATE_SESSION

@UnitTest
class CreateSessionRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def orderEntry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    def product = Mock([useObjenesis: false], ProductModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def usCountry = Mock([useObjenesis: false], CountryModel)

    def converter = new CreateSessionRequestConverter()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(CREATE_SESSION) >> new PaymentTransaction()
        populateOrderEntry()

        usCountry.isocode >> 'US'
        region.isocodeShort >> 'WI'
        billingAddress.country >> usCountry
        billingAddress.region >> region
        billingAddress.postalcode >> '19802'

        paymentInfo.billingAddress >> billingAddress

        currency.isocode >> 'USD'
        currency.digits >> 2
        cart.guid >> 'guid'
        cart.paymentInfo >> paymentInfo
        cart.currency >> currency
        cart.totalPrice >> 25
        cart.entries >> [orderEntry]
        cart.deliveryCost >> 15

        source.addParam('order', cart)
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
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.apSessionsServiceCancelURL == 'cancelURL'
        requestFields.apSessionsServiceFailureURL == 'failureURL'
        requestFields.apSessionsServiceSuccessURL == 'successURL'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 25
        requestFields.purchaseTotalsDiscountAmount == 10
        requestFields.purchaseTotalsTaxAmount == 1
        requestFields.billToCountry == 'US'
        requestFields.billToState == 'WI'
        requestFields.billToPostalCode == '19802'

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
