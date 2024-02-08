package isv.sap.payment.service.executor.request.converter.fraud

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

import static isv.cjl.payment.constants.PaymentServiceConstants.Fraud.ADVANCED_FRAUD_SCREEN
import static java.math.BigDecimal.TEN

@UnitTest
class AdvancedFraudScreenRequestConverterSpec extends Specification
{
    def converter = new AdvancedFraudScreenRequestConverter()

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def 'setup'()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(ADVANCED_FRAUD_SCREEN) >> paymentTransaction

        def currency = Mock([useObjenesis: false], CurrencyModel)
        currency.isocode >> 'GBP'

        def country = Mock([useObjenesis: false], CountryModel)
        country.isocode >> 'GB'

        def address = Mock([useObjenesis: false], AddressModel)
        address.firstname >> 'firstName'
        address.lastname >> 'lastName'
        address.email >> 'email@address.com'
        address.country >> country
        address.town >> 'town'

        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
        paymentInfo.billingAddress >> address

        def order = Mock([useObjenesis: false], AbstractOrderModel)
        order.guid >> 'guid'
        order.currency >> currency
        order.totalPrice >> 10D
        order.paymentInfo >> paymentInfo

        source.addParam('merchantId', 'merchantId')
        source.addParam('decisionManagerEnabled', true)
        source.addParam('order', order)
    }

    @Test
    def 'should convert payment service request to isv request'()
    {
        when:
        def target = converter.convert(source)

        then:
        target.requestFields['afsServiceRun'] == true
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['decisionManagerEnabled'] == true
        target.requestFields['purchaseTotalsCurrency'] == 'GBP'
        target.requestFields['billToFirstName'] == 'firstName'
        target.requestFields['billToLastName'] == 'lastName'
        target.requestFields['billToEmail'] == 'email@address.com'
        target.requestFields['billToCountry'] == 'GB'
        target.requestFields['billToCity'] == 'town'
        target.requestFields['purchaseTotalsGrandTotalAmount'] == TEN
    }
}
