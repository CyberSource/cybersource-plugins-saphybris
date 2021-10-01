package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.AUTHORIZATION_REVERSAL
import static isv.cjl.payment.enums.PaymentTransactionType.AUTHORIZATION

@UnitTest
class AuthorizationReversalRequestConverterSpec extends Specification
{
    def authorizationTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def source = PaymentServiceRequest.create()

    def converter = new AuthorizationReversalRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION_REVERSAL) >> paymentTransaction

        authorizationTransactionEntry.type >> AUTHORIZATION
        authorizationTransactionEntry.transactionStatus >> PaymentConstants.TransactionStatus.REVIEW
        authorizationTransactionEntry.requestId >> '00001'

        order.guid >> 'order_guid'
        order.currency >> [isocode: 'USD']
        order.totalPrice >> 100

        source.addParam('order', order)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('transaction', authorizationTransactionEntry)
        source.addParam('vcOrderId', '123456')
    }

    @Test
    def 'converter should create and populate VC authorization reversal request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantId'] == 'merchant_identifier'
        requestFields['merchantReferenceCode'] == 'order_guid'
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['vcOrderId'] == '123456'
        requestFields['paymentSolution'] == 'visacheckout'
        requestFields['purchaseTotalsGrandTotalAmount'] == 100
        requestFields['ccAuthServiceRun'] == true
        requestFields['ccAuthReversalServiceRun'] == true
        requestFields['ccAuthReversalServiceAuthRequestID'] == '00001'
    }
}
