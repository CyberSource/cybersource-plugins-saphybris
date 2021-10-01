package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.AUTHORIZATION

@UnitTest
class AuthorizationRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def converter = new AuthorizationRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION) >> paymentTransaction
        populateBillingAddress()

        paymentInfo.billingAddress >> billingAddress
        country.isocode >> 'GB'
        deliveryAddress.country >> country

        currency.isocode >> 'USD'
        cart.guid >> 'guid'
        cart.paymentInfo >> paymentInfo
        cart.deliveryAddress >> deliveryAddress
        cart.currency >> currency
        cart.totalPrice >> 25
        cart.deliveryCost >> 15

        source.addParam('order', cart)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('apAuthServicePreapprovalToken', 'pre-approval token')
    }

    @Test
    def 'should convert source to authorization request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'KLI'
        requestFields.apAuthServiceRun == true
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 25
        requestFields.apAuthServicePreapprovalToken == 'pre-approval token'

        assertBillToInfo(requestFields)
    }

    def populateBillingAddress()
    {
        country.isocode >> 'US'
        billingAddress.country >> country
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
        assert requestFields.billToPostalCode == '12345'
        assert requestFields.billToStreet1 == 'bill to address line 1'
    }
}
