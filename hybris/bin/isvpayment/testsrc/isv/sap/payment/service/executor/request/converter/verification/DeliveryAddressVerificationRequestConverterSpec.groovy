package isv.sap.payment.service.executor.request.converter.verification

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.Verification.DELIVERY_ADDRESS_VERIFICATION

@UnitTest
class DeliveryAddressVerificationRequestConverterSpec extends Specification
{
    def converter = new DeliveryAddressVerificationRequestConverter()

    def source = PaymentServiceRequest.create()

    def address = Mock(AddressModel)

    def country = Mock(CountryModel)

    def region = Mock(RegionModel)

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def 'setup'()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(DELIVERY_ADDRESS_VERIFICATION) >> paymentTransaction

        country.isocode >> 'US'
        region.isocode >> 'CA'

        address.town >> 'town'
        address.line1 >> 'line1'
        address.country >> country
        address.postalcode >> 'postalCode'

        source.addParam('merchantId', 'merchantId')
        source.addParam('merchantReferenceCode', 'merchantReferenceCode')
        source.addParam('address', address)
    }

    @Test
    def 'should convert payment service request'()
    {
        given:
        address.region >> region

        when:
        def fields = converter.convert(source).requestFields

        then:
        fields.merchantId == 'merchantId'
        fields.merchantReferenceCode == 'merchantReferenceCode'
        fields.davServiceRun == true
        fields.city == address.town
        fields.street1 == address.line1
        fields.country == address.country.isocode
        fields.state == address.region.isocode
        fields.postalCode == address.postalcode
    }

    @Test
    def 'should convert payment service request with empty region'()
    {
        given:
        address.region >> null

        when:
        def fields = converter.convert(source).requestFields

        then:
        fields.merchantId == 'merchantId'
        fields.merchantReferenceCode == 'merchantReferenceCode'
        fields.davServiceRun == true
        fields.city == address.town
        fields.street1 == address.line1
        fields.country == address.country.isocode
        fields.state == ''
        fields.postalCode == address.postalcode
    }
}
