package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.INITIATE
import static isv.cjl.payment.enums.AlternativePaymentMethod.APY

@UnitTest
class InitiateRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def converter = new InitiateRequestConverter()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(INITIATE) >> new PaymentTransaction()

        source.addParam('merchantId', 'merchantId')
        source.addParam('returnUrl', 'https://isv.com')
        source.addParam('apPaymentType', APY.code)
    }

    @Test
    def 'should convert source to initiate request'()
    {
        given:
        def currency = Mock([useObjenesis: false], CurrencyModel)
        def cart = Mock([useObjenesis: false], AbstractOrderModel)

        currency.isocode >> 'GBP'
        cart.guid >> 'guid'
        cart.currency >> currency
        cart.totalPrice >> 122
        cart.code >> '0022'

        source.addParam('order', cart)
        source.addParam('productName', cart.code)

        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'guid'
        target.requestFields['apInitiateServiceRun'] == true
        target.requestFields['apInitiateServiceProductName'] == '0022'
        target.requestFields['apPaymentType'] == APY.code
        target.requestFields['purchaseTotalsCurrency'] == 'GBP'
        target.requestFields['purchaseTotalsGrandTotalAmount'] == 122
    }
}
