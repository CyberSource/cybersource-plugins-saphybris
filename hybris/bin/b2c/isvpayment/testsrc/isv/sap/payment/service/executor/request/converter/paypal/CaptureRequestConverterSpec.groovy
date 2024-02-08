package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Unroll

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CAPTURE
import static isv.cjl.payment.enums.PaymentType.PAY_PAL

@UnitTest
class CaptureRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def source = PaymentServiceRequest.create()

    def converter = new CaptureRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CAPTURE) >> paymentTransaction

        currency.isocode >> 'GBP'
        order.guid >> 'order_code'
        order.currency >> currency
        order.totalPrice >> 12.5
        transaction.requestId >> 'AUTH_REQUEST_ID'

        source.method(PAY_PAL)
        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', merchant)
    }

    @Test
    @Unroll
    def 'Should create by default a Complete PayPal Capture Request'()
    {
        source.addParam('isCaptureFinal', isFinal)

        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid
        request.requestFields.apCaptureServiceRun == true
        request.requestFields.purchaseTotalsCurrency == 'GBP'
        request.requestFields.purchaseTotalsGrandTotalAmount == 12.5
        request.requestFields.apPaymentType == 'PPL'
        request.requestFields.apCaptureServiceAuthRequestID == 'AUTH_REQUEST_ID'
        request.requestFields.apCaptureServiceIsFinal == isFinalResult

        where:
        isFinal || isFinalResult
        true    || true
        false   || false
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
