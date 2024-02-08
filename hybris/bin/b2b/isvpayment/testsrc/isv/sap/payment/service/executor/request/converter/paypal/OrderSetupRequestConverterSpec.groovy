package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.ORDER_SETUP

@UnitTest
class OrderSetupRequestConverterSpec extends Specification
{
    def converter = new OrderSetupRequestConverter()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def source = new PaymentServiceRequest()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        def currency = Mock([useObjenesis: false], CurrencyModel)
        currency.isocode >> 'GBP'

        order.guid >> 'order_code'
        order.totalPrice >> Double.valueOf(100)
        order.currency >> currency

        transaction.requestId >> 'SESSIONS_REQUEST_ID'

        converter.requestFactory = requestFactory
        requestFactory.request(ORDER_SETUP) >> paymentTransaction

        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', 'test_hybris_2')
        source.addParam('PayerID', 'PAYER_ID')
    }

    @Test
    def 'should convert payment service request to isv request'()
    {
        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields.merchantId == source.requestParams.merchantId
        requestFields.merchantReferenceCode == order.guid
        requestFields.purchaseTotalsCurrency == order.currency.isocode
        requestFields.purchaseTotalsGrandTotalAmount == order.totalPrice
        requestFields.apPaymentType == 'PPL'
        requestFields.apOrderServiceRun == true
        requestFields.apPayerID == 'PAYER_ID'
    }

    @Test
    def 'should detect missing order'()
    {
        given:
        source.addParam('order', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
