package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.VOID

@UnitTest
class VoidRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentAddress = Mock([useObjenesis: false], AddressModel)

    def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def source = new PaymentServiceRequest()

    def converter = new VoidRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(VOID) >> paymentTransaction

        order.guid >> '123'
        order.paymentAddress >> paymentAddress
        order.currency >> [isocode: 'USD']
        entry.requestId >> 'entry_requestId'

        source.addParam('order', order)
        source.addParam('transaction', entry)
        source.addParam('vcOrderId', 'vcOrderId')
        source.addParam('merchantId', 'merchantId')
    }

    @Test
    def 'should populate specific fields'()
    {
        when:
        def requestFields = converter.convert(source).requestFields

        then:

        requestFields['merchantId'] == 'merchantId'
        requestFields['merchantReferenceCode'] == '123'
        requestFields['purchaseTotalsCurrency'] == 'USD'
        requestFields['vcOrderId'] == 'vcOrderId'
        requestFields['voidServiceVoidRequestID'] == 'entry_requestId'
        requestFields['voidServiceRun'] == true
        requestFields['paymentSolution'] == 'visacheckout'
    }
}
