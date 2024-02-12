package isv.sap.payment.service.executor.request.converter.applepay.strategies

import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.DecryptionType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.CARD_TYPE
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.DECRYPTION_TYPE
import static isv.cjl.payment.constants.PaymentServiceConstants.ApplePay.AUTHORIZATION
import static isv.cjl.payment.enums.DecryptionType.ISV_PAYMENT
import static isv.cjl.payment.enums.DecryptionType.MERCHANT

class MastercardMerchDecryptionStrategySpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def strategy = new MasterCardMerchDecryptionStrategy()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        factory.request(AUTHORIZATION) >> new PaymentTransaction()

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
        request.addParam(DECRYPTION_TYPE, ISV_PAYMENT)
        request.addParam(CARD_TYPE, CardType.MASTERCARD_EUROCARD)

        then:
        !strategy.supports(request)
    }

    @Test
    def 'Should not support request with incorrect card type'()
    {
        when:
        def request = PaymentServiceRequest.create()
        request.addParam(DECRYPTION_TYPE, MERCHANT)
        request.addParam(CARD_TYPE, CardType.DISCOVER)

        then:
        !strategy.supports(request)
    }

    @Test
    def 'Should support request'()
    {
        when:
        def request = PaymentServiceRequest.create()
        request.addParam(DECRYPTION_TYPE, MERCHANT)
        request.addParam(CARD_TYPE, CardType.MASTERCARD_EUROCARD)

        then:
        strategy.supports(request)
    }

    @Test
    def 'should convert source to request using merchant decryption'()
    {
        given:
        source.addParam('paymentToken', [
                'applicationPrimaryAccountNumber': '4817499130137812',
                'applicationExpirationDate'      : '231231',
                'currencyCode'                   : '826',
                'transactionAmount'              : 1885,
                'deviceManufacturerIdentifier'   : '040010030273',
                'paymentDataType'                : '3DSecure',
                'paymentData'                    : [
                        'onlinePaymentCryptogram': 'Amp3UzwACqPVG1/lbibCMAABAAA=',
                        'eciIndicator'           : '7'
                ]
        ])
        source.addParam('decryptionType', DecryptionType.MERCHANT)
        source.addParam('merchantId', 'merchId')
        source.addParam('cardType', CardType.MASTERCARD_EUROCARD)

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

        requestFields.cardAccountNumber == '4817499130137812'
        requestFields.cardExpirationMonth == '12'
        requestFields.cardExpirationYear == '23'
        requestFields.cardType == CardType.MASTERCARD_EUROCARD
        requestFields.ccAuthServiceCommerceIndicator == PaymentConstants.ApplePay.Authorization.MASTERCARD_COMMERCE_INDICATOR
        requestFields.ucafAuthenticationData == 'Amp3UzwACqPVG1/lbibCMAABAAA='
        requestFields.ucafCollectionIndicator == '2'
        requestFields.paymentNetworkTokenTransactionType == '1'

        requestFields.encryptedPaymentData == null
    }
}
