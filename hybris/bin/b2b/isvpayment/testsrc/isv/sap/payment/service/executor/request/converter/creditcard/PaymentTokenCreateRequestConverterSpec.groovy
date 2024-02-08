package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.enums.RecurringFrequency
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.Request
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.CREATE_TOKEN
import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION

@UnitTest
class PaymentTokenCreateRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def target = Mock([useObjenesis: false], Request)

    def converter = new PaymentTokenCreateRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CREATE_TOKEN) >> paymentTransaction

        source.paymentType = PaymentType.CREDIT_CARD
        source.paymentTransactionType = AUTHORIZATION

        source.addParam('merchantId', 'tacit_hybris_2')
        source.addParam('order', order)

        order.guid >> '00000001'

        transactionEntry.requestId >> '48481888'
        transactionEntry.requestToken >> 'DpESDdw4ZNX'
    }

    @Test
    def 'should create and populate payment CreateToken request'()
    {
        given:
        source.addParam('transaction', transactionEntry)

        when:
        def target = converter.convert(source)

        then:
        target != null
        target.requestFields['merchantId'] == 'tacit_hybris_2'
        target.requestFields['merchantReferenceCode'] == '00000001'
        target.requestFields['recurringSubscriptionInfoFrequency'] == RecurringFrequency.ON_DEMAND
        target.requestFields['paySubscriptionCreateServiceRun'] == true
        target.requestFields['paySubscriptionCreateServicePaymentRequestID'] == '48481888'
    }

    @Test
    def 'should throw exception if order payment authorization transaction not found'()
    {
        given:
        source.addParam('transaction', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
