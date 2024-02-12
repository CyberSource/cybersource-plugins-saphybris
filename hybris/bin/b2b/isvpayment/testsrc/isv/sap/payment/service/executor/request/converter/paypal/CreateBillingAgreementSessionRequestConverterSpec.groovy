package isv.sap.payment.service.executor.request.converter.paypal

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CREATE_BILLING_AGREEMENT_SESSION

@UnitTest
class CreateBillingAgreementSessionRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def cart = Mock([useObjenesis: false], AbstractOrderModel)

    def converter = new CreateBillingAgreementSessionRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(CREATE_BILLING_AGREEMENT_SESSION) >> paymentTransaction

        cart.guid >> 'guid'

        source.addParam('order', cart)
        source.addParam('merchantId', 'merchant_identifier')
        source.addParam('apSessionsServiceCancelURL', 'cancelURL')
        source.addParam('apSessionsServiceSuccessURL', 'successURL')
        source.addParam('apBillingAgreementIndicator', true)
    }

    @Test
    def 'should convert source to create billing agreement session request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'PPL'
        requestFields.apSessionsServiceRun == true
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
        requestFields.apSessionsServiceCancelURL == 'cancelURL'
        requestFields.apSessionsServiceSuccessURL == 'successURL'
        requestFields.apBillingAgreementIndicator == true
    }
}
