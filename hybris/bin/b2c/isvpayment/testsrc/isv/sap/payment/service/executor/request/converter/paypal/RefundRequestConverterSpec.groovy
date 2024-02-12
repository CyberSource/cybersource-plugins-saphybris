package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.REFUND

@UnitTest
class RefundRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

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
        source.addParam('transaction', transaction)
        source.addParam('amount', BigDecimal.TEN)

        order.guid >> '1234567890'
        order.currency >> [isocode: 'USD']

        transaction.requestId >> 'REFUND_REQUEST_ID'
    }

    @Test
    def 'converter should create and populate refund request'()
    {
        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields['merchantId'] == merchant
        requestFields['merchantReferenceCode'] == '1234567890'
        requestFields['apRefundServiceRun'] == true
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['purchaseTotalsGrandTotalAmount'] == BigDecimal.TEN
        requestFields['apRefundServiceRefundRequestID'] == 'REFUND_REQUEST_ID'
        requestFields['apPaymentType'] == 'PPL'
    }
}
