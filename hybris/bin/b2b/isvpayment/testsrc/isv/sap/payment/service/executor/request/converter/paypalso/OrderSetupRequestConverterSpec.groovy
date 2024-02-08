package isv.sap.payment.service.executor.request.converter.paypalso

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.ORDER_SETUP

@UnitTest
class OrderSetupRequestConverterSpec extends Specification
{
    def converter = new OrderSetupRequestConverter()

    def currency = new CurrencyModel()

    def order = new AbstractOrderModel()

    def source = new PaymentServiceRequest()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    void setup()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(ORDER_SETUP) >> paymentTransaction
        order.setGuid('order_code')
        order.setTotalPrice(Double.valueOf(100))
        order.setCurrency(currency)

        def paymentInfo = new PaymentInfoModel(billingAddress: new AddressModel(email: 'customer@email.com'))

        order.currency.isocode = 'GBP'
        order.paymentInfo = paymentInfo
        order.fingerPrintSessionID = 'id'

        source.addParam('order', order)
        source.addParam('merchantId', 'test_hybris_2')
    }

    @Test
    def 'should convert payment service request to isv request'()
    {
        given:
        def set = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.SET, properties: [:])
        def get = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.GET, properties: [:])

        set.requestId = 'requestId'
        set.requestToken = 'requestToken'
        set.properties.payPalEcSetReplyPaypalToken = 'token'
        get.properties.payPalEcGetDetailsReplyPayerId = 'payerId'

        source.addParam('set_transaction', set)
        source.addParam('get_transaction', get)

        when:
        def requestFields = converter.convert(source).requestFields

        then:
        requestFields.merchantId == source.requestParams.merchantId
        requestFields.merchantReferenceCode == order.guid
        requestFields.purchaseTotalsGrandTotalAmount == order.totalPrice
        requestFields.purchaseTotalsCurrency == order.currency.isocode
        requestFields.payPalEcOrderSetupServiceRun == true
        requestFields.payPalEcOrderSetupServicePaypalToken == set.properties.payPalEcSetReplyPaypalToken
        requestFields.payPalEcOrderSetupServicePaypalPayerId == get.properties.payPalEcGetDetailsReplyPayerId
        requestFields.payPalEcOrderSetupServicePaypalCustomerEmail == order.paymentInfo.billingAddress.email
        requestFields.payPalEcOrderSetupServicePaypalEcSetRequestID == set.requestId
        requestFields.payPalEcOrderSetupServicePaypalEcSetRequestToken == set.requestToken
        requestFields.deviceFingerprintID == 'id'
    }

    @Test
    def 'should detect missing set transaction entry'()
    {
        given:
        def get = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.GET, properties: [:])

        source.addParam('set_transaction', null)
        source.addParam('get_transaction', get)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should detect missing get transaction entry'()
    {
        given:
        def set = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.SET, properties: [:])

        source.addParam('set_transaction', set)
        source.addParam('get_transaction', null)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should detect missing token'()
    {
        def set = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.SET, properties: [:])
        def get = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.GET, properties: [:])

        set.requestId = 'requestId'
        set.requestToken = 'requestToken'
        get.properties.payPalEcGetDetailsReplyPayerId = 'payerId'

        source.addParam('set_transaction', set)
        source.addParam('get_transaction', get)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should detect missing payer identifier'()
    {
        given:
        def set = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.SET, properties: [:])
        def get = new IsvPaymentTransactionEntryModel(type: PaymentTransactionType.GET, properties: [:])

        set.requestId = 'requestId'
        set.requestToken = 'requestToken'
        set.properties.payPalEcSetReplyPaypalToken = 'token'

        source.addParam('set_transaction', set)
        source.addParam('get_transaction', get)

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }

    @Test
    def 'should detect missing payment info'()
    {
        given:
        order.paymentInfo = null

        when:
        converter.convert(source)

        then:
        thrown NullPointerException
    }
}
