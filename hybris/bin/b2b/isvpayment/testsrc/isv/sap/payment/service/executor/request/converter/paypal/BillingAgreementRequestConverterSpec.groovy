package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.BILLING_AGREEMENT
import static isv.cjl.payment.enums.PaymentType.PAY_PAL

@UnitTest
class BillingAgreementRequestConverterSpec extends IsvSpec
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transaction = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def source = PaymentServiceRequest.create()

    def converter = new BillingAgreementRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        order.guid >> 'order_code'
        transaction.requestId >> 'SESSIONS_REQUEST_ID'

        converter.requestFactory = requestFactory
        requestFactory.request(BILLING_AGREEMENT) >> paymentTransaction

        source.method(PAY_PAL)
        source.addParam('order', order)
        source.addParam('transaction', transaction)
        source.addParam('merchantId', merchant)
    }

    @Test
    def 'Should create a PayPal billing agreement Request'()
    {
        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid

        request.requestFields.apPaymentType == 'PPL'
        request.requestFields.apBillingAgreementServiceRun == true
        request.requestFields.apBillingAgreementServiceSessionsRequestID == 'SESSIONS_REQUEST_ID'
    }

    @Test
    def 'Should fail when no order is provided'()
    {
        given:
        source.addParam('order', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
