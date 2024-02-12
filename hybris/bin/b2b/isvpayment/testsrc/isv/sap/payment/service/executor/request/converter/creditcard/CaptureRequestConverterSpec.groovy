package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.executor.request.populator.Populator
import isv.cjl.payment.service.request.Request
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.CAPTURE

@UnitTest
class CaptureRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    @SuppressWarnings('BracesForClass')
    def processingLevelPopulator = new Populator<PaymentServiceRequest, PaymentTransaction>() {
        @Override
        void populate(PaymentServiceRequest paymentServiceRequest, PaymentTransaction paymentTransaction)
        {
        }
    }

    def target = Mock([useObjenesis: false], Request)

    def converter = new CaptureRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CAPTURE) >> paymentTransaction

        converter.processingLevelPopulator = processingLevelPopulator

        source.paymentTransactionType = PaymentTransactionType.AUTHORIZATION

        source.addParam('merchantId', 'tacit_hybris_2')
        source.addParam('order', order)
        source.addParam('transaction', transaction)

        currency.isocode >> 'EUR'

        order.guid >> '00000001'

        transaction.requestId >> '48481888'
        transaction.requestToken >> 'DpESDdw4ZNX'
        transaction.amount >> BigDecimal.TEN
        transaction.currency >> currency
    }

    @Test
    'should create and populate payment capture request'()
    {
        when:
        def target = converter.convert(source)

        then:
        target != null
        target.requestFields['purchaseTotalsCurrency'] == 'EUR'
        target.requestFields['merchantReferenceCode'] == '00000001'
        target.requestFields['ccCaptureServiceAuthRequestID'] == '48481888'
        target.requestFields['merchantId'] == 'tacit_hybris_2'
        target.requestFields['ccCaptureServiceAuthRequestToken'] == 'DpESDdw4ZNX'
        target.requestFields['ccCaptureServiceRun'] == true
        target.requestFields['purchaseTotalsGrandTotalAmount'] == 10
    }
}
