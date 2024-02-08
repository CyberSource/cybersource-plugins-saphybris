package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CANCEL_ORDER
import static isv.cjl.payment.enums.PaymentType.PAY_PAL

@UnitTest
class CancelOrderRequestConverterSpec extends IsvSpec
{
    def order = Mock(AbstractOrderModel)

    def transaction = Mock(PaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def converter = new CancelOrderRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        order.guid >> 'order_code'
        transaction.requestId >> 'ORDER_REQUEST_ID'

        converter.requestFactory = requestFactory
        requestFactory.request(CANCEL_ORDER) >> paymentTransaction

        source.method(PAY_PAL)
        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', merchant)
    }

    @Test
    def 'Should create a PayPal cancel order request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.apPaymentType == 'PPL'
        request.requestFields.apCancelServiceRun == true
        request.requestFields.apCancelServiceOrderRequestID == 'ORDER_REQUEST_ID'
    }

    @Test
    def 'Should create a PayPal cancel order request with billing agreement ID'()
    {
        given:
        source.addParam('apBillingAgreementID', 'apBillingAgreementValue')

        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.apPaymentType == 'PPL'
        request.requestFields.apCancelServiceRun == true
        request.requestFields.apCancelServiceOrderRequestID == null
        request.requestFields.apBillingAgreementID == 'apBillingAgreementValue'
    }

    @Test
    def 'Should fail when no order is provided'()
    {
        given:
        source.addParam('order', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
