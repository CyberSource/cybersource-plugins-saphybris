package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.DELETE_TOKEN
import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD

@UnitTest
class PaymentTokenDeleteRequestConverterSpec extends Specification
{
    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def source = new PaymentServiceRequest(paymentType: CREDIT_CARD)

    def converter = new PaymentTokenDeleteRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory

        requestFactory.request(DELETE_TOKEN) >> paymentTransaction

        def currency = Mock([useObjenesis: false], CurrencyModel)
        currency.isocode >> 'GBP'

        order.guid >> 'order_code'
        order.currency >> currency

        source.addParam('order', order)
        source.addParam('merchantId', 'tacit_hybris_2')

        transaction.subscriptionID >> 'subscriptionId'
    }

    @Test
    def 'should create delete token request'()
    {
        given:
        source.addParam('transaction', transaction)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields.merchantId == 'tacit_hybris_2'
        target.requestFields.merchantReferenceCode == order.guid
        target.requestFields.recurringSubscriptionInfoSubscriptionID == transaction.subscriptionID
        target.requestFields.paySubscriptionDeleteServiceRun == true
    }

    @Test
    def 'should throw exception when no transaction entry found for given status'()
    {
        given:
        source.addParam('transaction', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
