package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.ORDER_SETUP_REVERSAL

@UnitTest
class OrderSetupReversalRequestConverterSpec extends IsvSpec
{
    def order = new AbstractOrderModel(guid: 'order_code')

    def source = PaymentServiceRequest.create()

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def transactionProperties = [:]

    def converter = new OrderSetupReversalRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(ORDER_SETUP_REVERSAL) >> paymentTransaction
        source.method(PaymentType.PAY_PAL_SO)
        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)
        source.addParam('merchantId', merchant)

        transactionProperties.payPalEcOrderSetupReplyTransactionId = 'order_setUp_transaction_id_value'
        transactionEntry.properties >> transactionProperties
        transactionEntry.requestId >> 'order_setup_request_id_value'
        transactionEntry.requestToken >> 'order_setup_request_token'
    }

    @Test
    def 'Should create by default a Complete PayPal Order Setup Request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.payPalAuthReversalServicePaypalAuthorizationId == 'order_setUp_transaction_id_value'
        request.requestFields.payPalAuthReversalServicePaypalEcOrderSetupRequestID == 'order_setup_request_id_value'
        request.requestFields.payPalAuthReversalServicePaypalEcOrderSetupRequestToken == 'order_setup_request_token'
        request.requestFields.payPalAuthReversalServicePaypalAuthorizationRequestID == null
        request.requestFields.payPalAuthReversalServicePaypalAuthorizationRequestToken == null
    }

    @Test
    def 'Should fail when no PayPal Authorization transaction entry found'()
    {
        given:
        source.addParam('transaction', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
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
