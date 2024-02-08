package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.Request
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.SALE

@UnitTest
class SaleRequestConverterSpec extends Specification
{
    def converter = new SaleRequestConverter()

    def paymentServiceRequest = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)
    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def requestFactory = new RequestFactory()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(SALE) >> new PaymentTransaction()

        requestFactory.validators = []
    }

    @Test
    def 'Should throw exception if required parameter was not set'()
    {
        when:
        converter.convert(paymentServiceRequest)

        then:
        def ex = thrown(NullPointerException)
        ex.message == 'order is required param'
    }

    @Test
    def 'Should convert PaymentServiceRequest to valid Request for AP sale service'()
    {
        given:
        setUpCart()
        setUpPaymentServiceRequest()

        when:
        Request request = converter.convert(paymentServiceRequest)

        then:
        request.requestFields['merchantId'] == 'merchant-test'
        request.requestFields['merchantReferenceCode'] == '1234-5678'
        request.requestFields['apPaymentType'] == 'SOF'
        request.requestFields['apSaleServiceCancelURL'] == 'http://cancel'
        request.requestFields['apSaleServiceFailureURL'] == 'http://failure'
        request.requestFields['apSaleServiceSuccessURL'] == 'http://success'
        request.requestFields['apSaleServicePaymentOptionID'] == '789'
        request.requestFields['apSaleServiceRun'] == true
        request.requestFields['purchaseTotalsGrandTotalAmount'] == 123.4
        request.requestFields['purchaseTotalsCurrency'] == 'MDL'
        request.requestFields['billToFirstName'] == 'First Name'
        request.requestFields['billToLastName'] == 'Last Name'
        request.requestFields['billToCompany'] == 'Company'
        request.requestFields['billToEmail'] == 'email@email.com'
        request.requestFields['billToCountry'] == 'US'
        request.requestFields['billToCity'] == 'Town'
        request.requestFields['billToStreet1'] == 'Line 1'
        request.requestFields['billToState'] == 'AZ'
        request.requestFields['invoiceHeaderMerchantDescriptor'] == 'some merchant'
    }

    @Test
    def 'Should convert PaymentServiceRequest to valid Request for AP sale service with empty paymentInfo'()
    {
        given:
        cart.paymentInfo >> null
        setUpCart()
        setUpPaymentServiceRequest()

        when:
        Request request = converter.convert(paymentServiceRequest)

        then:
        request.requestFields['merchantId'] == 'merchant-test'
        request.requestFields['merchantReferenceCode'] == '1234-5678'
        request.requestFields['apPaymentType'] == 'SOF'
        request.requestFields['apSaleServiceCancelURL'] == 'http://cancel'
        request.requestFields['apSaleServiceFailureURL'] == 'http://failure'
        request.requestFields['apSaleServiceSuccessURL'] == 'http://success'
        request.requestFields['apSaleServiceRun'] == true
        request.requestFields['purchaseTotalsGrandTotalAmount'] == 123.4
        request.requestFields['purchaseTotalsCurrency'] == 'MDL'
        request.requestFields['billToFirstName'] == null
        request.requestFields['billToLastName'] == null
        request.requestFields['billToCompany'] == null
        request.requestFields['billToEmail'] == null
        request.requestFields['billToCountry'] == null
        request.requestFields['billToCity'] == null
        request.requestFields['billToStreet1'] == null
        request.requestFields['billToState'] == null
        request.requestFields['invoiceHeaderMerchantDescriptor'] == 'some merchant'
    }

    @Test
    def 'Should convert PaymentServiceRequest to valid Request for AP sale service with empty billing address'()
    {
        given:
        paymentInfo.billingAddress >> null
        setUpCart()
        setUpPaymentServiceRequest()

        when:
        Request request = converter.convert(paymentServiceRequest)

        then:
        request.requestFields['merchantId'] == 'merchant-test'
        request.requestFields['merchantReferenceCode'] == '1234-5678'
        request.requestFields['apPaymentType'] == 'SOF'
        request.requestFields['apSaleServiceCancelURL'] == 'http://cancel'
        request.requestFields['apSaleServiceFailureURL'] == 'http://failure'
        request.requestFields['apSaleServiceSuccessURL'] == 'http://success'
        request.requestFields['apSaleServiceRun'] == true
        request.requestFields['purchaseTotalsGrandTotalAmount'] == 123.4
        request.requestFields['purchaseTotalsCurrency'] == 'MDL'
        request.requestFields['billToFirstName'] == null
        request.requestFields['billToLastName'] == null
        request.requestFields['billToCompany'] == null
        request.requestFields['billToEmail'] == null
        request.requestFields['billToCountry'] == null
        request.requestFields['billToCity'] == null
        request.requestFields['billToStreet1'] == null
        request.requestFields['billToState'] == null
        request.requestFields['invoiceHeaderMerchantDescriptor'] == 'some merchant'
    }

    @Test
    def 'Should convert PaymentServiceRequest to valid Request for AP sale service with some empty values'()
    {
        given:
        Integer transactionTimeout = 100
        billingAddress.country >> null
        billingAddress.region >> null
        setUpCart()
        setUpPaymentServiceRequest()
        paymentServiceRequest.addParam('apSaleServicePaymentOptionID', null)
        paymentServiceRequest.addParam('apSaleServiceTransactionTimeout', transactionTimeout)

        when:
        Request request = converter.convert(paymentServiceRequest)

        then:
        request.requestFields['merchantId'] == 'merchant-test'
        request.requestFields['merchantReferenceCode'] == '1234-5678'
        request.requestFields['apPaymentType'] == 'SOF'
        request.requestFields['apSaleServiceCancelURL'] == 'http://cancel'
        request.requestFields['apSaleServiceFailureURL'] == 'http://failure'
        request.requestFields['apSaleServiceSuccessURL'] == 'http://success'
        request.requestFields['apSaleServicePaymentOptionID'] == null
        request.requestFields['apSaleServiceRun'] == true
        request.requestFields['purchaseTotalsGrandTotalAmount'] == 123.4
        request.requestFields['purchaseTotalsCurrency'] == 'MDL'
        request.requestFields['billToFirstName'] == 'First Name'
        request.requestFields['billToLastName'] == 'Last Name'
        request.requestFields['billToCompany'] == 'Company'
        request.requestFields['billToEmail'] == 'email@email.com'
        request.requestFields['billToCountry'] == null
        request.requestFields['billToCity'] == 'Town'
        request.requestFields['billToStreet1'] == 'Line 1'
        request.requestFields['billToState'] == null
        request.requestFields['invoiceHeaderMerchantDescriptor'] == 'some merchant'
        request.requestFields['apSaleServiceTransactionTimeout'] == transactionTimeout
    }

    def setUpPaymentServiceRequest()
    {
        paymentServiceRequest.addParam('order', cart)
        paymentServiceRequest.addParam('merchantId', 'merchant-test')
        paymentServiceRequest.addParam('apPaymentType', 'SOF')
        paymentServiceRequest.addParam('apSaleServiceCancelURL', 'http://cancel')
        paymentServiceRequest.addParam('apSaleServiceFailureURL', 'http://failure')
        paymentServiceRequest.addParam('apSaleServiceSuccessURL', 'http://success')
        paymentServiceRequest.addParam('apSaleServicePaymentOptionID', '789')
        paymentServiceRequest.addParam('invoiceHeaderMerchantDescriptor', 'some merchant')
    }

    def setUpCart()
    {
        cart.guid >> '1234-5678'
        cart.totalPrice >> 123.4
        CurrencyModel currency = Mock([useObjenesis: false], CurrencyModel)
        currency.isocode >> 'MDL'
        cart.currency >> currency

        paymentInfo.billingAddress >> billingAddress
        cart.paymentInfo >> paymentInfo

        billingAddress.firstname >> 'First Name'
        billingAddress.lastname >> 'Last Name'
        billingAddress.company >> 'Company'
        billingAddress.email >> 'email@email.com'

        CountryModel country = Mock([useObjenesis: false], CountryModel)
        country.isocode >> 'US'
        billingAddress.country >> country
        billingAddress.town >> 'Town'
        billingAddress.line1 >> 'Line 1'

        RegionModel region = Mock([useObjenesis: false], RegionModel)
        billingAddress.region >> region
        region.isocodeShort >> 'AZ'
    }
}
