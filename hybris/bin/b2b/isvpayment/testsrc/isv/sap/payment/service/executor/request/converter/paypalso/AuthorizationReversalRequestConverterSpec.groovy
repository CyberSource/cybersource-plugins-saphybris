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

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.AUTHORIZATION_REVERSAL

@UnitTest
class AuthorizationReversalRequestConverterSpec extends IsvSpec
{
    def order = new AbstractOrderModel(guid: 'order_code')

    def source = PaymentServiceRequest.create()

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def transactionProperties = [:]

    def converter = new AuthorizationReversalRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION_REVERSAL) >> paymentTransaction

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
    def 'Should create by default a Complete PayPal Authorization Reversal Request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.payPalAuthReversalServicePaypalAuthorizationId == 'authorization_id_value'
        request.requestFields.payPalAuthReversalServicePaypalAuthorizationRequestID == 'authorization_request_id_value'
        request.requestFields.payPalAuthReversalServicePaypalAuthorizationRequestToken == 'request_token'
        request.requestFields.payPalAuthReversalServicePaypalEcOrderSetupRequestID == null
        request.requestFields.payPalAuthReversalServicePaypalEcOrderSetupRequestToken == null
    }

    @Test
    def 'Should fail when no PayPal Authorization transaction entry found'()
    {
        given:
        source.addParam('order', null)

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
