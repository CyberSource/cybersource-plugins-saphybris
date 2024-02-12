package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.REFUND
import static isv.cjl.payment.enums.AlternativePaymentMethod.APY
import static isv.cjl.payment.enums.AlternativePaymentMethod.AYM
import static isv.cjl.payment.enums.AlternativePaymentMethod.IDL
import static isv.cjl.payment.enums.AlternativePaymentMethod.KLI
import static isv.cjl.payment.enums.AlternativePaymentMethod.MCH
import static isv.cjl.payment.enums.AlternativePaymentMethod.SOF
import static java.math.BigDecimal.TEN
import static org.apache.commons.lang.StringUtils.EMPTY

class RefundRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def converter = new RefundRequestConverter()

    def source = PaymentServiceRequest.create()

    def entry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(REFUND) >> new PaymentTransaction()

        order.guid >> 'guid'
        order.currency >> currency

        currency.isocode >> 'GBP'
        entry.requestId >> 'requestId'

        source.addParam('amount', TEN)
        source.addParam('merchantId', 'merchantId')
        source.addParam('order', order)
        source.addParam('reason', 'reason')
    }

    @Test
    def 'should convert source to payment refund service request for given alternative method'()
    {
        given:
        source.addParam('alternatePaymentMethod', apMethod)
        source.addParam('transaction', entry)

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields['merchantId'] == 'merchantId'
        requestFields['merchantReferenceCode'] == 'guid'
        requestFields['apPaymentType'] == apMethod.name()
        requestFields['apRefundServiceRun'] == true
        requestFields['apRefundServiceRefundRequestID'] == 'requestId'
        requestFields['apRefundServiceReason'] == reason
        requestFields['apRefundServiceApInitiateRequestID'] == apInitiateReqId
        requestFields['purchaseTotalsGrandTotalAmount'] == TEN
        requestFields['purchaseTotalsCurrency'] == 'GBP'

        where:
        apMethod | apInitiateReqId | reason
        IDL      | EMPTY           | EMPTY
        MCH      | EMPTY           | EMPTY
        SOF      | EMPTY           | EMPTY
        AYM      | 'requestId'     | 'reason'
        APY      | 'requestId'     | EMPTY
        KLI      | EMPTY           | EMPTY
    }

    @Test
    def 'should detect missing alternative payment method and throw relevant exception'()
    {
        when:
        converter.convert(source)

        then:
        thrown(NullPointerException)
    }
}
