package isv.sap.payment.service.executor.request.converter.applepay.strategies

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

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.AMEX_COMMERCE_INDICATOR
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.CARD_TYPE
import static isv.cjl.payment.constants.PaymentConstants.ApplePay.Authorization.DECRYPTION_TYPE
import static isv.cjl.payment.constants.PaymentServiceConstants.ApplePay.AUTHORIZATION
import static isv.cjl.payment.enums.DecryptionType.ISV_PAYMENT
import static isv.cjl.payment.enums.DecryptionType.MERCHANT

class AmexMerchDecryptionStrategySpec extends Specification
{

    def source = PaymentServiceRequest.create()

    def strategy = new AmexMerchDecryptionStrategy()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        factory.request(AUTHORIZATION) >> new PaymentTransaction()

        def order = Mock([useObjenesis: false], AbstractOrderModel)
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
        def billingAddress = Mock([useObjenesis: false], AddressModel)

        strategy.requestFactory = factory

        source.addParam('order', order)

        order.guid >> '123'
        paymentInfo.billingAddress >> billingAddress
        order.paymentInfo >> paymentInfo
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D

        billingAddress.firstname >> 'First'
        billingAddress.lastname >> 'Last'
        billingAddress.email >> 'sample@email.com'
        billingAddress.country >> [isocode: 'US']
        billingAddress.town >> 'San Francisco'
        billingAddress.postalcode >> '98111'
        billingAddress.region >> null
        billingAddress.line1 >> '5th'
        billingAddress.phone1 >> '123456789'
    }

    @Test
    def 'Should not support request with incorrect decryption type'()
    {
        when:
        def request = PaymentServiceRequest.create()
        request.addParam(DECRYPTION_TYPE, ISV_PAYMENT)
        request.addParam(CARD_TYPE, CardType.AMEX)

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
        request.addParam(CARD_TYPE, CardType.AMEX)

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
        source.addParam('cardType', CardType.AMEX)

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
        requestFields.billToState == null
        requestFields.billToStreet1 == '5th'

        requestFields.cardAccountNumber == '4817499130137812'
        requestFields.cardExpirationMonth == '12'
        requestFields.cardExpirationYear == '23'
        requestFields.cardType == CardType.AMEX
        requestFields.ccAuthServiceCavv == 'Amp3UzwACqPVG1/lbibCMAABAAA='
        requestFields.ccAuthServiceXid == null
        requestFields.ccAuthServiceCommerceIndicator == AMEX_COMMERCE_INDICATOR
        requestFields.paymentNetworkTokenTransactionType == '1'

        requestFields.encryptedPaymentData == null
    }

    @Test
    def 'should convert source to request having long cryptogram'()
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
                        'onlinePaymentCryptogram': 'Amp3UzwACqPVG1/lbibCMAABAAA123456789012=',
                        'eciIndicator'           : '7'
                ]
        ])
        source.addParam('decryptionType', DecryptionType.MERCHANT)
        source.addParam('merchantId', 'merchId')
        source.addParam('cardType', CardType.AMEX)

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
        requestFields.billToState == null
        requestFields.billToStreet1 == '5th'

        requestFields.cardAccountNumber == '4817499130137812'
        requestFields.cardExpirationMonth == '12'
        requestFields.cardExpirationYear == '23'
        requestFields.cardType == CardType.AMEX
        requestFields.ccAuthServiceCavv == 'Amp3UzwACqPVG1/lbibC'
        requestFields.ccAuthServiceXid == 'MAABAAA123456789012='
        requestFields.ccAuthServiceCommerceIndicator == AMEX_COMMERCE_INDICATOR
        requestFields.paymentNetworkTokenTransactionType == '1'

        requestFields.encryptedPaymentData == null
    }
}
