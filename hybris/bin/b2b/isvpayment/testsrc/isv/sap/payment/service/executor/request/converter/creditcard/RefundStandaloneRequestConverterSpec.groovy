package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.CreditCardType
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.dto.CardInfo
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.Month
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.executor.request.populator.Populator
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.REFUND_STANDALONE

@UnitTest
class RefundStandaloneRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def region = Mock([useObjenesis: false], RegionModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def cardInfo = Mock([useObjenesis: false], CardInfo)

    def converter = new RefundStandaloneRequestConverter()

    @SuppressWarnings('BracesForClass')
    def processingLevelPopulator = new Populator<PaymentServiceRequest, PaymentTransaction>() {
        @Override
        void populate(PaymentServiceRequest paymentServiceRequest, PaymentTransaction paymentTransaction)
        {
        }
    }

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(REFUND_STANDALONE) >> paymentTransaction

        source.addParam('merchantId', 'tacit_hybris_2')

        order.guid >> '123'
        order.paymentAddress >> billingAddress
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D

        order.guid >> '123'
        order.paymentAddress >> billingAddress
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D

        cardInfo.cardType >> CreditCardType.VISA
        cardInfo.cardNumber >> '4111111111111111'
        cardInfo.expirationMonth >> 10
        cardInfo.expirationYear >> 2017
        cardInfo.issueMonth >> 11
        cardInfo.issueYear >> 2015

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

        converter.processingLevelPopulator = processingLevelPopulator
    }

    @Test
    def 'creator should create and populate request object based on credit card'()
    {
        when:
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        source.addParam('card', cardInfo)

        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.merchantId == 'tacit_hybris_2'
        requestFields.merchantReferenceCode == '123'
        requestFields.ccCreditServiceRun == true
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 100

        requestFields.cardType == CardType.VISA
        requestFields.cardAccountNumber == '4111111111111111'
        requestFields.cardExpirationMonth == Month.OCT
        requestFields.cardExpirationYear == '2017'
        requestFields.cardStartMonth == Month.NOV
        requestFields.cardStartYear == '2015'

        requestFields.billToFirstName == 'First'
        requestFields.billToLastName == 'Last'
        requestFields.billToEmail == 'sample@email.com'
        requestFields.billToCountry == 'US'
        requestFields.billToCity == 'San Francisco'
        requestFields.billToPostalCode == '98111'
        requestFields.billToState == 'CA'
        requestFields.billToStreet1 == '5th'
        requestFields.billToStreet2 == 'Embarcadero'

        requestFields.recurringSubscriptionInfoSubscriptionID == null
    }

    @Test
    def 'creator should create and populate request object based on subscription id'()
    {
        when:
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        source.addParam('subscriptionID', '000123')

        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.merchantId == 'tacit_hybris_2'
        requestFields.merchantReferenceCode == '123'
        requestFields.ccCreditServiceRun == true
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 100

        requestFields.cardType == null
        requestFields.cardAccountNumber == null
        requestFields.cardExpirationMonth == null
        requestFields.cardExpirationYear == null
        requestFields.cardStartMonth == null
        requestFields.cardStartYear == null

        requestFields.billToFirstName == 'First'
        requestFields.billToLastName == 'Last'
        requestFields.billToEmail == 'sample@email.com'
        requestFields.billToCountry == 'US'
        requestFields.billToCity == 'San Francisco'
        requestFields.billToPostalCode == '98111'
        requestFields.billToState == 'CA'
        requestFields.billToStreet1 == '5th'
        requestFields.billToStreet2 == 'Embarcadero'

        requestFields.recurringSubscriptionInfoSubscriptionID == '000123'
    }

    @Test
    def 'should throw exception if order payment transaction entry not found'()
    {
        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should throw exception if card nor subscriptionID not found'()
    {
        given:
        source.addParam(PaymentConstants.CommonFields.ORDER, order)

        when:
        converter.convert(source)

        then:
        thrown RuntimeException
    }
}
