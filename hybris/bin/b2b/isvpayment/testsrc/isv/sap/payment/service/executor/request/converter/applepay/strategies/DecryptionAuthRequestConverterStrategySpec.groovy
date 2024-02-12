package isv.sap.payment.service.executor.request.converter.applepay.strategies

import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.DecryptionType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.cjl.payment.utils.PaymentHelper

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.CARD_TYPE
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.DECRYPTION_TYPE
import static isv.cjl.payment.constants.PaymentServiceConstants.ApplePay.AUTHORIZATION
import static isv.cjl.payment.enums.DecryptionType.ISV_PAYMENT
import static isv.cjl.payment.enums.DecryptionType.MERCHANT

class DecryptionAuthRequestConverterStrategySpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def strategy = new DecryptionAuthRequestConverterStrategy()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        factory.request(AUTHORIZATION) >> new PaymentTransaction()

        def paymentHelper = Mock([useObjenesis: false], PaymentHelper)
        paymentHelper.getCardType('Visa') >> CardType.VISA
        paymentHelper.getCommerceIndicator(CardType.VISA) >> 'internet'
        strategy.paymentHelper = paymentHelper

        def order = Mock([useObjenesis: false], AbstractOrderModel)
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
        def region = Mock([useObjenesis: false], RegionModel)
        def billingAddress = Mock([useObjenesis: false], AddressModel)

        strategy.requestFactory = factory

        source.addParam('order', order)

        order.guid >> '123'
        paymentInfo.billingAddress >> billingAddress
        order.paymentInfo >> paymentInfo
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D

        region.isocodeShort >> 'CA'

        billingAddress.firstname >> 'First'
        billingAddress.lastname >> 'Last'
        billingAddress.email >> 'sample@email.com'
        billingAddress.country >> [isocode: 'US']
        billingAddress.town >> 'San Francisco'
        billingAddress.postalcode >> '98111'
        billingAddress.region >> region
        billingAddress.line1 >> '5th'
        billingAddress.phone1 >> '123456789'
    }

    @Test
    def 'Should not support request with incorrect decryption type'()
    {
        when:
        def request = PaymentServiceRequest.create()
        request.addParam(DECRYPTION_TYPE, MERCHANT)
        request.addParam(CARD_TYPE, CardType.AMEX)

        then:
        !strategy.supports(request)
    }

    @Test
    def 'Should support request'()
    {
        when:
        def request = PaymentServiceRequest.create()
        request.addParam(DECRYPTION_TYPE, ISV_PAYMENT)
        request.addParam(CARD_TYPE, CardType.AMEX)

        then:
        strategy.supports(request)
    }

    @Test
    def 'should convert source to request using merchant decryption'()
    {
        given:
        source.addParam('paymentToken', [
                'token': [
                        'paymentData'          : [
                                'version'  : 'EC_v1',
                                'data'     :
                                        'BpHNF32cyNPqvwGYB2oVW2/hWYc2F+InVn9HHqpy8AdGhjzxBEGX93DVns7CakR4rrANLHc5v6aKAQsdGufA/ZfBImUqh/AoYPPAeArrVXNrBUGdq5yyqoFWtxiSY78f1UYW4kEujTyGJU4Qh5qX/rqfYX51yf/DiYMoPms/Y5YgJyUxTrKzZKuDi9m44ldUCXYDKEoV1Hco8M8MnxH4+1lB2GajeEw66mGqkBrxpEFrU2Ns9wowsJQPgr3b9uTPVi0K8cgm3m7XWnvsDTMgAD5MXfHNjSxY4H9xkWEh4gLz9TXruXPpHTVswkYtqS3/MKiwcD0tLUbfPQ39LivDc2dgAlhvuaK61K2EXyxdzk4SPIKyk+kjz84zU9jfh4AFtMH5hlBXmHzNxlB1AsLHe7hlF9Z69qyiuPJhk6tpCPk=',
                                'signature':
                                        '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',
                                'header'   : [
                                        'ephemeralPublicKey':
                                                'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERieH9N7vk9kX/s/kfOuY/eb6KXVUFs5j4p4mJKiMyuQqbUorcPpzetdoPtwFMb5iJe9iDCyAX4vLydHUnZgptw==',
                                        'publicKeyHash'     : 't/4JCIsHy3qxUF96eHYNBv4X/Cu+xGO9+lEwoOKV+G4=',
                                        'transactionId'     : '5D9A31609F862A96223D2B99AB11382A2EABF1F91D7DDD783F3377ECFBD9BA19'
                                ]
                        ],
                        'paymentMethod'        : [
                                'displayName': 'Visa 0492',
                                'network'    : 'Visa',
                                'type'       : 'debit'
                        ],
                        'transactionIdentifier': '5D9A31609F862A96223D2B99AB11382A2EABF1F91D7DDD783F3377ECFBD9BA19'
                ]
        ])
        source.addParam('decryptionType', DecryptionType.ISV_PAYMENT)
        source.addParam('merchantId', 'merchId')

        when:
        def requestFields = strategy.convert(source).requestFields

        then:
        requestFields.merchantId == 'merchId'
        requestFields.merchantReferenceCode == '123'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == BigDecimal.valueOf(100D)
        requestFields.ccAuthServiceRun == true
        requestFields.paymentSolution == '001'

        requestFields.billToCity == 'San Francisco'
        requestFields.billToCountry == 'US'
        requestFields.billToEmail == 'sample@email.com'
        requestFields.billToFirstName == 'First'
        requestFields.billToLastName == 'Last'
        requestFields.billToPostalCode == '98111'
        requestFields.billToState == 'CA'
        requestFields.billToStreet1 == '5th'

        requestFields.encryptedPaymentData != null
        requestFields.encryptedPaymentDescriptor == 'RklEPUNPTU1PTi5BUFBMRS5JTkFQUC5QQVlNRU5U'
        requestFields.encryptedPaymentEncoding == 'Base64'
        requestFields.paymentNetworkTokenTransactionType == '1'

        requestFields.cardAccountNumber == null
    }
}
