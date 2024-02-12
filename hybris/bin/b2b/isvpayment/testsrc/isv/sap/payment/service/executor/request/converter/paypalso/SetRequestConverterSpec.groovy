package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.SET

@UnitTest
class SetRequestConverterSpec extends IsvSpec
{
    def order = Mock(AbstractOrderModel)

    def transactionEntry = Mock(IsvPaymentTransactionEntryModel)

    def deliveryMode = Mock(DeliveryModeModel)

    def billingAddress = Mock(AddressModel)

    def shippingAddress = Mock(AddressModel)

    def paymentInfo = Mock(PaymentInfoModel)

    def source = PaymentServiceRequest.create()

    def converter = new SetRequestConverter()

    def requestFactory = Mock(RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(SET) >> paymentTransaction

        source.addParam('set_transaction', transactionEntry)
        source.addParam('merchantId', merchant)
        source.addParam('order', order)
        source.addParam(PaymentConstants.PayPalServiceRequestFields.RETURN_URL, 'http://local:9002/return')
        source.addParam(PaymentConstants.PayPalServiceRequestFields.CANCEL_URL, 'http://local:9002/cancel')

        deliveryMode.name >> 'USPS'

        order.guid >> '1234567890'
        order.deliveryAddress >> shippingAddress
        paymentInfo.billingAddress >> billingAddress
        order.paymentInfo >> paymentInfo
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 21D
        order.deliveryMode >> deliveryMode
        order.deliveryCost >> 0.8D
        order.deliveryCost >> 0.8D

        transactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.ACCEPT
        transactionEntry.requestId >> '5544332211'
        transactionEntry.requestToken >> '4433221100'
        transactionEntry.properties >> ['payPalEcSetReplyPaypalToken': '778899001122']

        billingAddress.firstname >> 'John'
        billingAddress.lastname >> 'Smith'
        billingAddress.phone1 >> '3456789012'
        billingAddress.email >> 'jsmith@email.com'
        billingAddress.country >> [isocode: 'US']
        billingAddress.town >> 'San Francisco'
        billingAddress.postalcode >> '98111'
        billingAddress.region >> [isocodeShort: 'CA']
        billingAddress.line1 >> 'Embarcadero'
        billingAddress.line2 >> '5th'

        shippingAddress.firstname >> 'Borea'
        shippingAddress.lastname >> 'Borisov'
        shippingAddress.phone1 >> '6789012345'
        shippingAddress.email >> 'bborisov@email.com'
        shippingAddress.company >> 'Levis'
        shippingAddress.country >> [isocode: 'US']
        shippingAddress.town >> 'New York'
        shippingAddress.postalcode >> '91111'
        shippingAddress.region >> [isocodeShort: 'NY']
        shippingAddress.district >> 'New York'
        shippingAddress.line1 >> 'Broadway'
        shippingAddress.line2 >> '15th'
        shippingAddress.building >> '2nd'
    }

    @Test
    def 'converter should create and populate request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['payPalEcSetServiceRun'] == true
        requestFields['payPalEcSetServicePaypalReturn'] == 'http://local:9002/return'
        requestFields['payPalEcSetServicePaypalCancelReturn'] == 'http://local:9002/cancel'
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D

        requestFields['payPalEcSetServicePaypalCustomerEmail'] == 'jsmith@email.com'
        requestFields['payPalEcSetServicePaypalEcSetRequestID'] == '5544332211'
        requestFields['payPalEcSetServicePaypalEcSetRequestToken'] == '4433221100'
        requestFields['payPalEcSetServicePaypalToken'] == '778899001122'

        requestFields['billToCity'] == 'San Francisco'
        requestFields['billToCountry'] == 'US'
        requestFields['billToEmail'] == 'jsmith@email.com'
        requestFields['billToFirstName'] == 'John'
        requestFields['billToLastName'] == 'Smith'
        requestFields['billToPhoneNumber'] == '3456789012'
        requestFields['billToPostalCode'] == '98111'
        requestFields['billToState'] == 'CA'
        requestFields['billToStreet1'] == 'Embarcadero'
        requestFields['billToStreet2'] == '5th'

        requestFields['shipToFirstName'] == 'Borea'
        requestFields['shipToLastName'] == 'Borisov'
        requestFields['shipToShippingMethod'] == 'USPS'
        requestFields['shipToPhoneNumber'] == '6789012345'
        requestFields['shipToCity'] == 'New York'
        requestFields['shipToCompany'] == 'Levis'
        requestFields['shipToCountry'] == 'US'
        requestFields['shipToCounty'] == 'New York'
        requestFields['shipToPostalCode'] == '91111'
        requestFields['shipToState'] == 'NY'
        requestFields['shipToStreet1'] == 'Broadway'
        requestFields['shipToStreet2'] == '15th'
        requestFields['shipToStreet3'] == '2nd'
    }
}
