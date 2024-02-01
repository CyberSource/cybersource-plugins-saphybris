package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.CAPTURE

@UnitTest
class CaptureRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def converter = new CaptureRequestConverter()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(CAPTURE) >> new PaymentTransaction()

        currency.isocode >> 'USD'
        order.guid >> 'guid'
        order.currency >> currency
        order.totalPrice >> 25
        transactionEntry.requestId >> 'auth-request-identifier'

        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)
        source.addParam('merchantId', 'merchant_identifier')
    }

    @Test
    def 'should convert source to capture request'()
    {
        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields.apPaymentType == 'KLI'
        requestFields.apCaptureServiceRun == true
        requestFields.apCaptureServiceAuthRequestID == 'auth-request-identifier'
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.purchaseTotalsCurrency == 'USD'
        requestFields.purchaseTotalsGrandTotalAmount == 25
    }
}
