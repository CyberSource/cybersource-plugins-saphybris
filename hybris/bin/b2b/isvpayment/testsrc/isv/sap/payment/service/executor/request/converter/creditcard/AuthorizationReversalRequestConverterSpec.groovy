package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.AUTHORIZATION_REVERSAL
import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION

@UnitTest
class AuthorizationReversalRequestConverterSpec extends Specification
{

    def authorizationTransactionEntry1 = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationReversalRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION_REVERSAL) >> paymentTransaction

        authorizationTransactionEntry1.type >> AUTHORIZATION
        authorizationTransactionEntry1.transactionStatus >> PaymentConstants.TransactionStatus.REVIEW
        authorizationTransactionEntry1.requestId >> '00001'

        order.guid >> '123'
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100D

        source.addParam('order', order)
        source.addParam('merchantId', 'tacit_hybris_2')
        source.addParam('transaction', authorizationTransactionEntry1)
        source.method(PaymentType.CREDIT_CARD)
    }

    @Test
    def 'creator should create and populate request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        target == paymentTransaction.request

        requestFields['merchantId'] == 'tacit_hybris_2'
        requestFields['merchantReferenceCode'] == '123'
        requestFields['ccAuthReversalServiceRun'] == true
        requestFields['ccAuthReversalServiceAuthRequestID'] == '00001'
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['purchaseTotalsGrandTotalAmount'] == 100
    }
}
