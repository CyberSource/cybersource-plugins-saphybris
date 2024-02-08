package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.AUTHORIZATION_REVERSAL
import static isv.cjl.payment.enums.PaymentType.PAY_PAL

@UnitTest
class AuthorizationReversalRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationReversalRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        order.guid >> 'order_code'
        transaction.requestId >> 'authorization_id_value'

        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION_REVERSAL) >> paymentTransaction

        source.method(PAY_PAL)
        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', merchant)
    }

    @Test
    def 'Should create by default a Complete PayPal Authorization Reversal Request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid
        request.requestFields.apPaymentType == AlternativePaymentMethod.PPL.code
        request.requestFields.apAuthReversalServiceRun == true
        request.requestFields.apAuthReversalServiceAuthRequestID == 'authorization_id_value'
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
