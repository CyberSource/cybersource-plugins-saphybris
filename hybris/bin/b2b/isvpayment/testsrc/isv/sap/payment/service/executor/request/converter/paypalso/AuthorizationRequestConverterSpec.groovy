package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.AUTHORIZATION

@UnitTest
class AuthorizationRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def getTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def orderSetupTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION) >> paymentTransaction

        source.addParam('get_transaction', getTransactionEntry)
        source.addParam('order_setup_transaction', orderSetupTransactionEntry)
        source.addParam('merchantId', merchant)
        source.addParam('order', order)

        order.guid >> '1234567890'
        order.paymentInfo >> paymentInfo
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 21D
        order.deliveryCost >> 0.8D
        order.deliveryCost >> 0.8D
        order.fingerPrintSessionID >> 'id'

        getTransactionEntry.properties >> [payPalEcGetDetailsReplyPayer: 'replyPayer']

        orderSetupTransactionEntry.requestId >> '5544332211'
        orderSetupTransactionEntry.requestToken >> '4433221100'
        orderSetupTransactionEntry.properties >> ['payPalEcOrderSetupReplyTransactionId': '332211556677889900']

        billingAddress.firstname >> 'John'
        billingAddress.lastname >> 'Smith'
        billingAddress.district >> 'Brooklyn'
        billingAddress.phone1 >> '3456789012'
        billingAddress.email >> 'jsmith@email.com'
        billingAddress.country >> [isocode: 'US']
        billingAddress.company >> 'Isv LLC'
        billingAddress.town >> 'San Francisco'
        billingAddress.postalcode >> '98111'
        billingAddress.region >> [isocodeShort: 'CA']
        billingAddress.line1 >> 'Embarcadero'
        billingAddress.line2 >> '5th'
        billingAddress.building >> 'bl.4'
    }

    @Test
    def 'converter should create and populate request object'()
    {
        given:
        paymentInfo.billingAddress >> billingAddress

        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['payPalAuthorizationServiceRun'] == true
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['payPalAuthorizationServicePaypalCustomerEmail'] == 'replyPayer'
        requestFields['payPalAuthorizationServicePaypalEcOrderSetupRequestID'] == '5544332211'
        requestFields['payPalAuthorizationServicePaypalEcOrderSetupRequestToken'] == '4433221100'
        requestFields['payPalAuthorizationServicePaypalOrderId'] == '332211556677889900'

        requestFields['billToCity'] == 'San Francisco'
        requestFields['billToCompany'] == 'Isv LLC'
        requestFields['billToCountry'] == 'US'
        requestFields['billToCounty'] == 'Brooklyn'
        requestFields['billToEmail'] == 'jsmith@email.com'
        requestFields['billToFirstName'] == 'John'
        requestFields['billToLastName'] == 'Smith'
        requestFields['billToPhoneNumber'] == '3456789012'
        requestFields['billToPostalCode'] == '98111'
        requestFields['billToState'] == 'CA'
        requestFields['billToStreet1'] == 'Embarcadero'
        requestFields['billToStreet2'] == '5th'
        requestFields['billToStreet3'] == 'bl.4'
        requestFields['deviceFingerprintID'] == 'id'
    }

    @Test
    def 'converter should create and populate request object without billing info'()
    {
        given:
        paymentInfo.billingAddress >> null

        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['payPalAuthorizationServiceRun'] == true
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['payPalAuthorizationServicePaypalCustomerEmail'] == 'replyPayer'
        requestFields['payPalAuthorizationServicePaypalEcOrderSetupRequestID'] == '5544332211'
        requestFields['payPalAuthorizationServicePaypalEcOrderSetupRequestToken'] == '4433221100'
        requestFields['payPalAuthorizationServicePaypalOrderId'] == '332211556677889900'

        requestFields['billToCity'] == null
        requestFields['billToCompany'] == null
        requestFields['billToCountry'] == null
        requestFields['billToCounty'] == null
        requestFields['billToEmail'] == null
        requestFields['billToFirstName'] == null
        requestFields['billToLastName'] == null
        requestFields['billToPhoneNumber'] == null
        requestFields['billToPostalCode'] == null
        requestFields['billToState'] == null
        requestFields['billToStreet1'] == null
        requestFields['billToStreet2'] == null
        requestFields['billToStreet3'] == null
        requestFields['deviceFingerprintID'] == 'id'
    }
}
