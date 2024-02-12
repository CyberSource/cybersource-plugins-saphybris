package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.REFUND

@UnitTest
class RefundRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def source = PaymentServiceRequest.create()

    def converter = new RefundRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(REFUND) >> paymentTransaction

        source.addParam('merchantId', merchant)
        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)

        order.guid >> '1234567890'
        order.currency >> [isocode: 'USD']

        transactionEntry.requestId >> '5544332211'
        transactionEntry.requestToken >> '4433221100'
        transactionEntry.properties >> ['payPalDoCaptureReplyTransactionId': '332211556677889900']

        billingAddress.firstname >> 'John'
        billingAddress.lastname >> 'Smith'
        billingAddress.email >> 'jsmith@email.com'
    }

    @Test
    def 'converter should create and populate full refund request'()
    {
        given:
        order.totalPrice >> 21D
        order.paymentAddress >> billingAddress

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D
        assertRequestFields(requestFields)
    }

    @Test
    def 'converter should create and populate full refund request without billing address'()
    {
        given:
        order.totalPrice >> 21D
        order.paymentAddress >> null

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['payPalRefundServiceRun'] == true
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['payPalRefundServicePaypalDoCaptureRequestID'] == '5544332211'
        requestFields['payPalRefundServicePaypalDoCaptureRequestToken'] == '4433221100'
        requestFields['payPalRefundServicePaypalCaptureId'] == '332211556677889900'

        requestFields['billToEmail'] == null
        requestFields['billToFirstName'] == null
    }

    @Test
    def 'converter should create and populate partial refund request'()
    {
        given:
        source.addParam('amount', BigDecimal.TEN)
        order.paymentAddress >> billingAddress

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields['purchaseTotalsGrandTotalAmount'] == BigDecimal.TEN
        assertRequestFields(requestFields)
    }

    def assertRequestFields(def requestFields)
    {
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['payPalRefundServiceRun'] == true
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['payPalRefundServicePaypalDoCaptureRequestID'] == '5544332211'
        requestFields['payPalRefundServicePaypalDoCaptureRequestToken'] == '4433221100'
        requestFields['payPalRefundServicePaypalCaptureId'] == '332211556677889900'

        requestFields['billToEmail'] == 'jsmith@email.com'
        requestFields['billToFirstName'] == 'John'
        requestFields['billToLastName'] == 'Smith'
    }
}
