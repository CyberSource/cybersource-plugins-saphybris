package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.PayPalSOCaptureTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.CAPTURE

@UnitTest
class CaptureRequestConverterSpec extends IsvSpec
{
    def order = new AbstractOrderModel(guid: 'order_code')

    def currency = new CurrencyModel()

    def source = PaymentServiceRequest.create()

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def transactionProperties = [:]

    def converter = new CaptureRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CAPTURE) >> paymentTransaction
        currency.setIsocode('GBP')
        order.setCurrency(currency)
        order.totalPrice = 12.5

        source.method(PaymentType.PAY_PAL_SO)
        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)
        source.addParam('merchantId', merchant)

        transactionProperties.payPalAuthorizationReplyTransactionId = 'authorization_id_value'
        transactionEntry.properties >> transactionProperties
        transactionEntry.requestId >> 'authorization_request_id_value'
        transactionEntry.requestToken >> 'request_token'
    }

    @Test
    def 'Should create by default a Complete PayPal Capture Request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.payPalDoCaptureServiceCompleteType == PayPalSOCaptureTransactionType.COMPLETE
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationId == 'authorization_id_value'
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationRequestID == 'authorization_request_id_value'
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationRequestToken == 'request_token'
        request.requestFields.purchaseTotalsCurrency == 'GBP'
        request.requestFields.purchaseTotalsGrandTotalAmount == 12.5
        request.requestFields.payPalDoCaptureServiceRun == true
    }

    @Test
    def 'Should create by Partial PayPal Capture Request'()
    {
        given:
        source.addParam('isCompleteCapture', false)

        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.payPalDoCaptureServiceCompleteType == PayPalSOCaptureTransactionType.NOT_COMPLETE
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationId == 'authorization_id_value'
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationRequestID == 'authorization_request_id_value'
        request.requestFields.payPalDoCaptureServicePaypalAuthorizationRequestToken == 'request_token'
        request.requestFields.purchaseTotalsCurrency == 'GBP'
        request.requestFields.purchaseTotalsGrandTotalAmount == 12.5
        request.requestFields.payPalDoCaptureServiceRun == true
    }

    @Test
    def 'Should fail when no order is provided '()
    {
        given:
        source.addParam('order', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
