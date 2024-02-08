package isv.sap.payment.service.executor.request.converter.reporting

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

@UnitTest
class ReportingTransactionRequestConverterSpec extends Specification
{
    private static final String MERCHANT_ID = 'tacit_hybris_2'

    public static final String MERCHANT_REFERENCE_CODE = '123'

    def order = Mock(AbstractOrderModel)

    def source = PaymentServiceRequest.create()

    def converter = new ReportingTransactionRequestConverter()

    def requestFactory = Mock(RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request() >> paymentTransaction

        order.guid >> MERCHANT_REFERENCE_CODE

        source.addParam('merchantId', MERCHANT_ID)
        source.addParam('order', order)
    }

    @Test
    def 'creator should create and populate request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        target == paymentTransaction.request

        requestFields['merchantId'] == MERCHANT_ID
        requestFields['merchantReferenceCode'] == MERCHANT_REFERENCE_CODE
    }
}
