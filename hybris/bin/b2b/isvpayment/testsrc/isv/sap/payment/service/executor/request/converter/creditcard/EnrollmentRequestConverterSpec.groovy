package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.CreditCardType
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.dto.CardInfo
import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.TransactionMode
import isv.cjl.payment.exception.PaymentException
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import org.junit.Test
import spock.lang.Specification

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.CARD
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYER_AUTH_ENROLL_SERVICE_REFERENCE_ID
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.ENROLLMENT
import static isv.cjl.payment.enums.Month.OCT

@UnitTest
class EnrollmentRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def address = Mock([useObjenesis: false], AddressModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def cardInfo = Mock([useObjenesis: false], CardInfo)

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def converter = new EnrollmentRequestConverter(requestFactory: requestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        source.addParam(PAYER_AUTH_ENROLL_SERVICE_REFERENCE_ID, 'ref_id')

        requestFactory.request(ENROLLMENT) >> paymentTransaction

        order.guid >> '123'
        order.currency >> [isocode: 'USD']
        order.paymentInfo >> paymentInfo
        order.deliveryAddress >> address
        order.totalPrice >> 100

        cardInfo.cardType >> CreditCardType.VISA
        cardInfo.cardNumber >> '4111111111111111'
        cardInfo.expirationMonth >> 10
        cardInfo.expirationYear >> 2017

        paymentInfo.billingAddress >> address

        region.isocodeShort >> 'CA'

        address.firstname >> 'First'
        address.lastname >> 'Last'
        address.email >> 'sample@email.com'
        address.country >> [isocode: 'US']
        address.town >> 'San Francisco'
        address.postalcode >> '98111'
        address.region >> region
        address.line1 >> '5th'
        address.line2 >> 'Embarcadero'
        address.phone1 >> '123456789'

        source.addParam('merchantId', 'tacit_hybris_2')
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        source.addParam('card', cardInfo)
        source.addParam('payerAuthEnrollServiceReferenceID', '123')
        source.addParam('payerAuthEnrollServiceTransactionMode', TransactionMode.ECOMMERCE)
    }

    def 'converter should create and populate request object with card info'()
    {
        given:
        source.addParam(CARD, cardInfo)
        when:
        def fields = converter.convert(source).requestFields

        then:
        fields['merchantId'] == 'tacit_hybris_2'
        fields['merchantReferenceCode'] == '123'
        fields['payerAuthEnrollServiceRun'] == true
        fields['ccAuthServiceRun'] == true
        fields['ccAuthServiceCardTypeSelectionIndicator'] == 1
        fields['purchaseTotalsCurrency'] == 'USD'
        fields['purchaseTotalsGrandTotalAmount'] == 100

        fields['recurringSubscriptionInfoSubscriptionID'] == null
        fields['payerAuthEnrollServiceTransactionMode'] == TransactionMode.ECOMMERCE
        fields['payerAuthEnrollServiceReferenceID'] == '123'

        fields['cardType'] == CardType.VISA
        fields['cardAccountNumber'] == '4111111111111111'
        fields['cardExpirationMonth'] == OCT
        fields['cardExpirationYear'] == '2017'
        fields['cardStartMonth'] == null
        fields['cardStartYear'] == null

        fields['billToFirstName'] == 'First'
        fields['billToLastName'] == 'Last'
        fields['billToEmail'] == 'sample@email.com'
        fields['billToCountry'] == 'US'
        fields['billToCity'] == 'San Francisco'
        fields['billToPostalCode'] == '98111'
        fields['billToState'] == 'CA'
        fields['billToStreet1'] == '5th'
        fields['billToStreet2'] == 'Embarcadero'

        fields['shipToFirstName'] == 'First'
        fields['shipToLastName'] == 'Last'
        fields['shipToCountry'] == 'US'
        fields['shipToCity'] == 'San Francisco'
        fields['shipToPostalCode'] == '98111'
        fields['shipToState'] == 'CA'
        fields['shipToStreet1'] == '5th'
        fields['shipToStreet2'] == 'Embarcadero'
    }

    def 'converter should create and populate request object with flex transient token'()
    {
        given:
        source.addParam('flexToken', 'test_flex_token')

        when:
        def fields = converter.convert(source).requestFields

        then:
        fields['merchantId'] == 'tacit_hybris_2'
        fields['merchantReferenceCode'] == '123'
        fields['payerAuthEnrollServiceRun'] == true
        fields['purchaseTotalsCurrency'] == 'USD'

        fields['cardAccountNumber'] == null
        fields['payerAuthEnrollServiceTransactionMode'] == TransactionMode.ECOMMERCE
        fields['payerAuthEnrollServiceReferenceID'] == '123'

        fields['tokenSourceTransientToken'] == 'test_flex_token'

        fields['billToFirstName'] == 'First'
        fields['billToLastName'] == 'Last'
        fields['billToEmail'] == 'sample@email.com'
        fields['billToCountry'] == 'US'
        fields['billToCity'] == 'San Francisco'
        fields['billToPostalCode'] == '98111'
        fields['billToState'] == 'CA'
        fields['billToStreet1'] == '5th'
        fields['billToStreet2'] == 'Embarcadero'

        fields['shipToFirstName'] == 'First'
        fields['shipToLastName'] == 'Last'
        fields['shipToCountry'] == 'US'
        fields['shipToCity'] == 'San Francisco'
        fields['shipToPostalCode'] == '98111'
        fields['shipToState'] == 'CA'
        fields['shipToStreet1'] == '5th'
        fields['shipToStreet2'] == 'Embarcadero'
    }

    @Test
    def 'should throw exception when flex token and card info not provided'()
    {
        when:
        source.addParam('flexToken', null)
        source.addParam('card', null)
        converter.convert(source)

        then:
        PaymentException exception = thrown()
        exception.message == 'one of card info or flex transient token must be supplied'
    }
}
