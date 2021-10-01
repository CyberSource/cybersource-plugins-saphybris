package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.VOID

@UnitTest
class VoidRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def converter = new VoidRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(VOID) >> paymentTransaction

        source.addParam('merchantId', 'merchant')
        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('vcOrderId', '_vcOrderId')

        order.guid >> 'order_guid'
        order.paymentAddress >> billingAddress

        billingAddress.email >> 'jsmith@mail.com'
        billingAddress.firstname >> 'john'
        billingAddress.lastname >> 'smith'

        transaction.requestId >> 'transaction_requestId'
        transaction.requestToken >> '3456789012'
    }

    @Test
    def 'converter should create valid request for void transaction operation'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['paymentSolution'] == 'visacheckout'
        requestFields['merchantId'] == 'merchant'
        requestFields['vcOrderId'] == '_vcOrderId'
        requestFields['merchantReferenceCode'] == 'order_guid'
        requestFields['voidServiceRun'] == true
        requestFields['voidServiceVoidRequestID'] == 'transaction_requestId'
    }
}
