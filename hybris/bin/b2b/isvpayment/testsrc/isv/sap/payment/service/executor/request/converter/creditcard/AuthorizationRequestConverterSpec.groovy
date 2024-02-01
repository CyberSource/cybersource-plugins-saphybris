package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.CreditCardType
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.dto.CardInfo
import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.Month
import isv.cjl.payment.exception.PaymentException
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import org.junit.Test
import spock.lang.Specification

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.AUTHORIZATION

@UnitTest
class AuthorizationRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def cardInfo = Mock([useObjenesis: false], CardInfo)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def converter = new AuthorizationRequestConverter(requestFactory: requestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        requestFactory.request(AUTHORIZATION) >> paymentTransaction

        order.guid >> '123'
        paymentInfo.billingAddress >> billingAddress
        order.paymentInfo >> paymentInfo
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D
        order.fingerPrintSessionID >> 'id'

        cardInfo.cardType >> CreditCardType.VISA
        cardInfo.cardNumber >> '4111111111111111'
        cardInfo.cv2Number >> '123'
        cardInfo.expirationMonth >> 10
        cardInfo.expirationYear >> 2017

        region.isocodeShort >> 'CA'

        billingAddress.firstname >> 'First'
        billingAddress.lastname >> 'Last'
        billingAddress.email >> 'sample@email.com'
        billingAddress.country >> [isocode: 'US']
        billingAddress.town >> 'San Francisco'
        billingAddress.postalcode >> '98111'
        billingAddress.region >> region
        billingAddress.line1 >> '5th'
        billingAddress.line2 >> 'Embarcadero'

        source.addParam('order', order)
        source.addParam('card', cardInfo)
        source.addParam('merchantId', 'tacit_hybris_2')
    }

    @Test
    def 'creator should create and populate request object using card info'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        target == paymentTransaction.request

        requestFields['merchantId'] == 'tacit_hybris_2'
        requestFields['merchantReferenceCode'] == '123'
        requestFields['ccAuthServiceRun'] == true
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['purchaseTotalsGrandTotalAmount'] == 100

        requestFields['cardType'] == CardType.VISA
        requestFields['cardAccountNumber'] == '4111111111111111'
        requestFields['cardCvNumber'] == null
        requestFields['cardExpirationMonth'] == Month.OCT
        requestFields['cardExpirationYear'] == '2017'
        requestFields['cardStartMonth'] == null
        requestFields['cardStartYear'] == null

        requestFields['billToFirstName'] == 'First'
        requestFields['billToLastName'] == 'Last'
        requestFields['billToEmail'] == 'sample@email.com'
        requestFields['billToCountry'] == 'US'
        requestFields['billToCity'] == 'San Francisco'
        requestFields['billToPostalCode'] == '98111'
        requestFields['billToState'] == 'CA'
        requestFields['billToStreet1'] == '5th'
        requestFields['billToStreet2'] == 'Embarcadero'
        requestFields['deviceFingerprintID'] == 'id'
    }

    @Test
    def 'should set flex transient token instead of card info if provided'()
    {
        when:
        source.addParam('flexToken', 'test_flex_token')
        source.addParam('authServiceCommerceIndicator', 'recurring')
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['tokenSourceTransientToken'] == 'test_flex_token'
        requestFields['ccAuthServiceCommerceIndicator'] == 'recurring'
        requestFields['cardType'] == null
        requestFields['cardAccountNumber'] == null
    }

    @Test
    def 'should set subscription id instead of card info if provided'()
    {
        when:
        source.addParam('subscriptionID', '1234567890')
        source.addParam('card', null)
        source.addParam('flexToken', null)
        source.addParam('authServiceCommerceIndicator', 'recurring')
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['recurringSubscriptionInfoSubscriptionID'] == '1234567890'
        requestFields['ccAuthServiceCommerceIndicator'] == 'recurring'
        requestFields['cardType'] == null
        requestFields['cardAccountNumber'] == null
        requestFields['tokenSourceTransientToken'] == null
    }

    @Test
    def 'creator should populate authorization response if provided'()
    {
        when:
        source.addParam('paRes', 'eNqdmElz6kg')
        source.addParam('payerAuthValidateServiceAuthenticationTransactionID', 'agdnBshdgs')
        source.addParam('payerAuthValidateServiceRun', true)
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['payerAuthValidateServiceRun'] == true
        requestFields['payerAuthValidateServiceSignedPARes'] == 'eNqdmElz6kg'
        requestFields['payerAuthValidateServiceAuthenticationTransactionID'] == 'agdnBshdgs'
    }

    @Test
    def 'creator should populate decision manager fields if provided'()
    {
        given:
        if (dmEnabled)
        {
            source.addParam('decisionManagerEnabled', dmEnabled)
        }

        when:
        def target = converter.convert(source)

        then:
        def requestFields = target.requestFields
        def dmEnabledParam = requestFields['decisionManagerEnabled']
        dmEnabled ? dmEnabledParam == dmEnabled : dmEnabledParam == null

        where:
        dmEnabled << [null, Boolean.FALSE, Boolean.TRUE]
    }

    @Test
    def 'should throw exception when no flex token or card info or subscription ID provided'()
    {
        when:
        source.addParam('flexToken', null)
        source.addParam('card', null)
        source.addParam('subscriptionID', null)
        converter.convert(source)

        then:
        PaymentException exception = thrown()
        exception.message == 'one of card info ,flex transient token or subscription ID must be supplied'
    }
}
