package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CHECK_STATUS

@UnitTest
class CheckStatusRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def converter = new CheckStatusRequestConverter()

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        order.guid >> 'MERCH_CODE'
        transaction.requestId >> 'SESSIONS_REQUEST_ID'

        converter.requestFactory = requestFactory
        requestFactory.request(CHECK_STATUS) >> paymentTransaction

        source.addParam('merchantId', 'merchantId')
        source.addParam('order', order)
        source.addParam('transaction', transaction)
    }

    @Test
    def 'should convert source to PayPal check status request including billing agreement ID'()
    {
        given:
        source.addParam('apBillingAgreementID', 'AGREEMENT_ID')

        when:
        def target = converter.convert(source)

        then:
        assertCommonValues(target)
        target.requestFields['apBillingAgreementID'] == 'AGREEMENT_ID'
    }

    @Test
    def 'should convert source to PayPal check status request including sessions request ID'()
    {
        given:
        source.addParam('transaction', transaction)

        when:
        def target = converter.convert(source)

        then:
        assertCommonValues(target)
        target.requestFields['apCheckStatusServiceSessionsRequestID'] == 'SESSIONS_REQUEST_ID'
    }

    def assertCommonValues(def target)
    {
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'MERCH_CODE'
        target.requestFields['apPaymentType'] == AlternativePaymentMethod.PPL.code
        target.requestFields['apCheckStatusServiceRun'] == true
    }
}
