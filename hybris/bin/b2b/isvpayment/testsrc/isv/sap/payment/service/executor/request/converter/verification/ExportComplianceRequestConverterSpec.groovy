package isv.sap.payment.service.executor.request.converter.verification

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.Verification.EXPORT_COMPLIANCE
import static isv.cjl.payment.enums.ExportComplianceAddressOperator.IGNORE
import static isv.cjl.payment.enums.ExportComplianceFieldWeight.HIGH
import static isv.cjl.payment.enums.ExportComplianceFieldWeight.LOW
import static isv.cjl.payment.enums.ExportComplianceFieldWeight.MEDIUM
import static org.apache.commons.lang.StringUtils.EMPTY

@UnitTest
class ExportComplianceRequestConverterSpec extends Specification
{
    def converter = new ExportComplianceRequestConverter()

    def source = PaymentServiceRequest.create()

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def deliveryAddress = Mock([useObjenesis: false], AddressModel)

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def country = Mock([useObjenesis: false], CountryModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def 'setup'()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(EXPORT_COMPLIANCE) >> paymentTransaction

        region.isocodeShort >> EMPTY >> 'IL'
        country.isocode >> 'UK' >> 'US'

        billingAddress.town >> 'London'
        billingAddress.country >> country
        billingAddress.email >> 'some@mail.com'
        billingAddress.line1 >> 'bill to address line 1'
        billingAddress.line2 >> 'bill to address line 2'
        billingAddress.company >> 'company'
        billingAddress.firstname >> 'John'
        billingAddress.lastname >> 'Doe'
        billingAddress.postalcode >> '1111'
        billingAddress.region >> region

        deliveryAddress.town >> 'Chicago'
        deliveryAddress.country >> country
        deliveryAddress.firstname >> 'Dave'
        deliveryAddress.lastname >> 'Miller'
        deliveryAddress.postalcode >> '1234'
        deliveryAddress.line1 >> 'ship to address line 1'
        deliveryAddress.line2 >> 'ship to address line 2'
        deliveryAddress.region >> region

        paymentInfo.billingAddress >> billingAddress

        cart.paymentInfo >> paymentInfo
        cart.deliveryAddress >> deliveryAddress
        cart.guid >> 'merchantReferenceCode'

        source.addParam('merchantId', 'merchantId')
        source.addParam('order', cart)
        source.addParam('exportServiceAddressOperator', IGNORE)
        source.addParam('exportServiceAddressWeight', LOW)
        source.addParam('exportServiceCompanyWeight', MEDIUM)
        source.addParam('exportServiceNameWeight', HIGH)
    }

    @Test
    def 'should convert payment service request'()
    {
        when:
        def fields = converter.convert(source).requestFields

        then:
        fields.merchantId == 'merchantId'
        fields.merchantReferenceCode == 'merchantReferenceCode'
        fields.exportServiceRun == true

        fields.billToCity == 'London'
        fields.billToCountry == 'UK'
        fields.billToEmail == 'some@mail.com'
        fields.billToStreet1 == 'bill to address line 1'
        fields.billToStreet2 == 'bill to address line 2'
        fields.billToCompany == 'company'
        fields.billToFirstName == 'John'
        fields.billToLastName == 'Doe'
        fields.billToPostalCode == '1111'
        fields.billToState == EMPTY

        fields.shipToCity == 'Chicago'
        fields.shipToCountry == 'US'
        fields.shipToFirstName == 'Dave'
        fields.shipToLastName == 'Miller'
        fields.shipToStreet1 == 'ship to address line 1'
        fields.shipToStreet2 == 'ship to address line 2'
        fields.shipToPostalCode == '1234'
        fields.shipToState == 'IL'

        fields.exportServiceAddressOperator == IGNORE
        fields.exportServiceAddressWeight == LOW
        fields.exportServiceCompanyWeight == MEDIUM
        fields.exportServiceNameWeight == HIGH
    }
}
