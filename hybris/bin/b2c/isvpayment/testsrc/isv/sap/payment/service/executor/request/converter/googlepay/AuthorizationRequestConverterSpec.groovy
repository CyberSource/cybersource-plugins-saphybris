package isv.sap.payment.service.executor.request.converter.googlepay

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.CardType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.cjl.payment.utils.PaymentHelper
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentRequestParamConstants.*
import static isv.cjl.payment.constants.PaymentServiceConstants.GooglePay.AUTHORIZATION
import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.encryptedExpiredPaymentToken
import static isv.sap.payment.integration.helpers.GooglePayTransactionsCreator.expiredPaymentData

@UnitTest
class AuthorizationRequestConverterSpec extends IsvSpec
{
    def order = Mock(AbstractOrderModel)

    def region = Mock(RegionModel)

    def billingAddress = Mock(AddressModel)

    def paymentInfo = Mock(PaymentInfoModel)

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock(RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def paymentHelper = Mock(PaymentHelper)

    def converter = new AuthorizationRequestConverter(paymentHelper: paymentHelper)

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION) >> paymentTransaction

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
        billingAddress.line2 >> 'Embarcadero'

        paymentHelper.getCardType('VISA') >> CardType.VISA
    }

    @Test
    def 'converter should create and populate request object'()
    {
        given:
        source.addParam('order', order)
        source.addParam('merchantId', merchant)
        source.addParam('paymentData', expiredPaymentData)
        source.addParam('order', order)
        paymentHelper.getCardType('VISA') >> CardType.VISA

        when:
        def fields = converter.convert(source).requestFields

        then:
        def billingAddress = order.getPaymentInfo().getBillingAddress()
        fields[MERCHANT_ID] == merchant
        fields[MERCHANT_REFERENCE_CODE] == order.guid
        fields[BILL_TO_FIRST_NAME] == billingAddress.firstname
        fields[BILL_TO_LAST_NAME] == billingAddress.lastname
        fields[BILL_TO_STREET1] == billingAddress.line1
        fields[BILL_TO_STREET2] == billingAddress.line2
        fields[BILL_TO_CITY] == billingAddress.town
        fields[BILL_TO_STATE] == billingAddress.region.isocodeShort
        fields[BILL_TO_POSTAL_CODE] == billingAddress.postalcode
        fields[BILL_TO_COUNTRY] == billingAddress.country.isocode
        fields[BILL_TO_EMAIL] == billingAddress.email
        fields[BILL_TO_PHONE_NUMBER] == billingAddress.phone1
        fields[PURCHASE_TOTALS_CURRENCY] == order.currency.isocode
        fields[PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT] == order.totalPrice
        fields[ENCRYPTED_PAYMENT_DATA] == encryptedExpiredPaymentToken
        fields[CARD_TYPE] == CardType.VISA
        fields[CC_AUTH_SERVICE_RUN] == true
        fields[PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE] == GOOGLE_PAY_PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE_VALUE
        fields[PAYMENT_SOLUTION] == GOOGLE_PAY_PAYMENT_SOLUTION_VALUE
    }
}
