package isv.sap.payment.service.executor.request.converter.creditcard

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

import isv.cjl.payment.configuration.service.ConfigurationService
import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.TaxCalculation.TAX
import static isv.sap.payment.constants.IsvPaymentConstants.ProductCodeProperties.SELLER_REGISTRATION_CODE
import static isv.sap.payment.constants.IsvPaymentConstants.ProductCodeProperties.TAX_DEFAULT_PRODUCT_CODE
import static isv.sap.payment.constants.IsvPaymentConstants.ProductCodeProperties.TAX_SHIPPING_PRODUCT_CODE

class TaxRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def orderEntry = Mock([useObjenesis: false], AbstractOrderEntryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def address = Mock([useObjenesis: false], AddressModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def product = Mock([useObjenesis: false], ProductModel)

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def converter = new TaxRequestConverter(requestFactory: requestFactory)

    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(TAX) >> paymentTransaction

        paymentInfo.billingAddress >> address

        order.deliveryAddress >> address
        order.paymentInfo >> paymentInfo
        order.entries >> [orderEntry]
        order.guid >> '1234567890'
        order.currency >> currency
        order.deliveryCost >> 5.5

        source.addParam('order', order)
        source.addParam('merchantId', 'merchant')

        orderEntry.product >> product
        orderEntry.quantity >> 2
        orderEntry.totalPrice >> 50
        orderEntry.order >> order

        product.code >> '232323'
        product.name >> 'product'

        currency.isocode >> 'GBP'
        currency.digits >> 2
        country.isocode >> 'GB'

        address.country >> country
        address.company >> 'company'
        address.town >> 'city'
        address.district >> 'district'
        address.email >> 'jsmith@mail.com'
        address.firstname >> 'john'
        address.lastname >> 'smith'
        address.phone1 >> '0987654321'
        address.postalcode >> '54321'
        address.region >> region
        address.line1 >> 'nr.1'
        address.line2 >> 'str. name'
        address.building >> 'bl.2'

        region.isocodeShort >> 'CA'

        converter.configurationService = configurationService
    }

    @Test
    def 'converter should create request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['purchaseTotalsCurrency'] == 'GBP'
        requestFields['taxServiceRun'] == true

        requestFields['billToCountry'] == 'GB'
        requestFields['billToCity'] == 'city'
        requestFields['billToCompany'] == 'company'
        requestFields['billToEmail'] == 'jsmith@mail.com'
        requestFields['billToFirstName'] == 'john'
        requestFields['billToLastName'] == 'smith'
        requestFields['billToPhoneNumber'] == '0987654321'
        requestFields['billToPostalCode'] == '54321'
        requestFields['billToState'] == 'CA'
        requestFields['billToStreet1'] == 'nr.1'
        requestFields['billToStreet2'] == 'str. name'
        requestFields['billToStreet3'] == 'bl.2'

        requestFields['shipToBuildingNumber'] == 'bl.2'
        requestFields['shipToCity'] == 'city'
        requestFields['shipToCompany'] == 'company'
        requestFields['shipToCountry'] == 'GB'
        requestFields['shipToDistrict'] == 'district'
        requestFields['shipToFirstName'] == 'john'
        requestFields['shipToLastName'] == 'smith'
        requestFields['shipToPhoneNumber'] == '0987654321'
        requestFields['shipToState'] == 'CA'
        requestFields['shipToPostalCode'] == '54321'
        requestFields['shipToStreet1'] == 'nr.1'
        requestFields['shipToStreet2'] == 'str. name'
        requestFields['shipToStreet3'] == 'bl.2'

        requestFields['taxServiceSellerRegistration'] == 'seller reg code'

        requestFields['0:itemId'] == 0
        requestFields['0:itemProductCode'] == 'product tax code'
        requestFields['0:itemProductName'] == 'product'
        requestFields['0:itemProductSKU'] == '232323'
        requestFields['0:itemQuantity'] == 2
        requestFields['0:itemUnitPrice'] == 25.0

        requestFields['1:itemId'] == 1
        requestFields['1:itemProductCode'] == 'ship tax code'
        requestFields['1:itemProductName'] == 'SHIPPING'
        requestFields['1:itemProductSKU'] == 'SHIPPING'
        requestFields['1:itemQuantity'] == 1
        requestFields['1:itemUnitPrice'] == 5.5

        1 * configurationService.getString(SELLER_REGISTRATION_CODE) >> 'seller reg code'
        1 * configurationService.getRequiredString(TAX_SHIPPING_PRODUCT_CODE) >> 'ship tax code'
        1 * configurationService.getRequiredString(TAX_DEFAULT_PRODUCT_CODE) >> 'product tax code'
    }
}
