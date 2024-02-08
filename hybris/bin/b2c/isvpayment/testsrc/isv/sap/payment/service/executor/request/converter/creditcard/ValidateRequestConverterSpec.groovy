package isv.sap.payment.service.executor.request.converter.creditcard

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.dto.CardInfo
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.constants.PaymentConstants
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.VALIDATE

@UnitTest
class ValidateRequestConverterSpec extends Specification
{
    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def cardInfo = Mock([useObjenesis: false], CardInfo)

    def source = PaymentServiceRequest.create()

    def converter = new ValidateRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(VALIDATE) >> paymentTransaction

        order.guid >> '123'
        order.currency >> [isocode: 'USD']

        cardInfo.cardNumber >> '4111111111111111'
        cardInfo.expirationMonth >> 10
        cardInfo.expirationYear >> 2017

        source.addParam('merchantId', 'tacit_hybris_2')
        source.addParam(PaymentConstants.CommonFields.ORDER, order)
        source.addParam('paRes', 'ABCDEF')
    }

    @Test
    def 'creator should create and populate request object'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields['merchantId'] == 'tacit_hybris_2'
        requestFields['merchantReferenceCode'] == '123'
        requestFields['payerAuthValidateServiceRun'] == true
        requestFields['purchaseTotalsCurrency'] == 'USD'

        requestFields['payerAuthValidateServiceSignedPARes'] == 'ABCDEF'
    }
}
