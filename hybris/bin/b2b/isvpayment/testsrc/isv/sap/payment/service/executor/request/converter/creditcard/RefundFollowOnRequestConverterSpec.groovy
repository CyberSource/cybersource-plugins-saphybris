package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.executor.request.populator.Populator
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.REFUND_FOLLOW_ON
import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD
import static java.math.BigDecimal.TEN

@UnitTest
class RefundFollowOnRequestConverterSpec extends Specification
{
    def transactionEntry = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    @SuppressWarnings('BracesForClass')
    def processingLevelPopulator = new Populator<PaymentServiceRequest, PaymentTransaction>() {
        @Override
        void populate(PaymentServiceRequest paymentServiceRequest, PaymentTransaction paymentTransaction)
        {
        }
    }

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def source = new PaymentServiceRequest()

    def converter = new RefundFollowOnRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(REFUND_FOLLOW_ON) >> paymentTransaction

        order.guid >> 'order_code'
        order.currency >> currency

        source.addParam('order', order)
        source.addParam('amount', TEN)
        source.addParam('merchantId', 'tacit_hybris_2')
        source.paymentType = CREDIT_CARD

        converter.processingLevelPopulator = processingLevelPopulator
    }

    @Test
    def 'should create refund follow-on request'()
    {
        given:

        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields.merchantId == 'tacit_hybris_2'
        target.requestFields.purchaseTotalsGrandTotalAmount == TEN
        target.requestFields.merchantReferenceCode == order.guid
        target.requestFields.purchaseTotalsCurrency == order.currency.isocode
        target.requestFields.ccCreditServiceCaptureRequestID == transactionEntry.requestId
        target.requestFields.ccCreditServiceRun == true
    }

    @Test
    def 'should throw exception if order payment transaction entry not found'()
    {
        given:
        source.addParam('transaction', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
