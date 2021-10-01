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

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.AUTHORIZATION

@UnitTest
class AuthorizationRequestConverterSpec extends IsvSpec
{
    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationRequestConverter()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION) >> paymentTransaction

        source.addParam('orderRequestID', 'ORDER_REQUEST_ID')
        source.addParam('merchantId', merchant)
        source.addParam('order', order)
        source.addParam('transaction', transaction)

        order.guid >> '1234567890'
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 21D
        transaction.requestId >> 'ORDER_REQUEST_ID'
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
        requestFields['apAuthServiceRun'] == true
        requestFields['apPaymentType'] == AlternativePaymentMethod.PPL.code
        requestFields['apAuthServiceOrderRequestID'] == 'ORDER_REQUEST_ID'
        requestFields['purchaseTotalsGrandTotalAmount'] == 21D
        requestFields['purchaseTotalsCurrency'] == 'USD'
    }
}
