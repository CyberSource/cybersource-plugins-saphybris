package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.integration.helpers.IsvSpec
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.GET

@UnitTest
class GetRequestConverterSpec extends IsvSpec
{
    def order = new AbstractOrderModel(guid: 'order_code')

    def source = PaymentServiceRequest.create()

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def transactionProperties = [:]

    def converter = new GetRequestConverter()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(GET) >> paymentTransaction
        source.method(PaymentType.PAY_PAL_SO)
        source.addParam('order', order)
        source.addParam('merchantId', merchant)
        transactionProperties.payPalEcSetReplyPaypalToken = 'paypal_token'
        transactionEntry.properties >> transactionProperties
        transactionEntry.requestId >> 'request_id_value'
        transactionEntry.requestToken >> 'request_token_value'
    }

    @Test
    def 'should create a PayPal GET request'()
    {
        given:
        source.addParam(SET_TRANSACTION, transactionEntry)

        when:
        def request = converter.convert(source)

        then:
        request.requestFields.merchantId == merchant
        request.requestFields.merchantReferenceCode == order.guid
        request.requestFields.payPalEcGetDetailsServicePaypalEcSetRequestID == 'request_id_value'
        request.requestFields.payPalEcGetDetailsServicePaypalEcSetRequestToken == 'request_token_value'
        request.requestFields.payPalEcGetDetailsServicePaypalToken == 'paypal_token'
        request.requestFields.payPalEcGetDetailsServiceRun == true
    }

    @Test
    def 'should throw exception when no PayPal transaction found'()
    {
        given:
        source.addParam(SET_TRANSACTION, null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should throw exception when order is not set'()
    {
        given:
        source.addParam(SET_TRANSACTION, transactionEntry)
        source.addParam('order', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should throw exception when merchant not set'()
    {
        given:
        source.addParam(SET_TRANSACTION, transactionEntry)
        source.addParam('merchantId', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should throw exception when paypalToken not set'()
    {
        given:
        source.addParam(SET_TRANSACTION, transactionEntry)
        transactionProperties.payPalEcSetReplyPaypalToken = null

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
