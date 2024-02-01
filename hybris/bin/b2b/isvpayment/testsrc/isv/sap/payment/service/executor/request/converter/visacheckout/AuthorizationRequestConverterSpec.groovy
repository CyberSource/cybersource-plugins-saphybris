package isv.sap.payment.service.executor.request.converter.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.AUTHORIZATION

@UnitTest
class AuthorizationRequestConverterSpec extends Specification
{
    def converter = new AuthorizationRequestConverter()

    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(AUTHORIZATION) >> paymentTransaction

        currency.isocode >> 'USD'
        order.guid >> 'guid'
        order.totalPrice >> 123.45
        order.currency >> currency

        source.addParam('merchantId', 'merchantIdentifier')
        source.addParam('vcOrderId', 'visaCheckoutOrderIdentifier')
        source.addParam('order', order)
    }

    @Test
    def 'should convert source to visa checkout authorization request'()
    {
        when:
        def reqFields = converter.convert(source).requestFields

        then:
        reqFields['vcOrderId'] == 'visaCheckoutOrderIdentifier'
        reqFields['paymentSolution'] == 'visacheckout'
        reqFields['merchantId'] == 'merchantIdentifier'
        reqFields['merchantReferenceCode'] == 'guid'
        reqFields['ccAuthServiceRun'] == true
        reqFields['purchaseTotalsCurrency'] == 'USD'
        reqFields['purchaseTotalsGrandTotalAmount'] == 123.45
    }
}
