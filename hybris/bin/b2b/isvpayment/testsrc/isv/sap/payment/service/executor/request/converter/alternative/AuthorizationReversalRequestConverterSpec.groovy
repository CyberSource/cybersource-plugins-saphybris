package isv.sap.payment.service.executor.request.converter.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.AUTHORIZATION_REVERSAL

@UnitTest
class AuthorizationReversalRequestConverterSpec extends Specification
{
    def source = PaymentServiceRequest.create()

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def billingAddress = Mock([useObjenesis: false], AddressModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def converter = new AuthorizationReversalRequestConverter()

    def factory = Mock([useObjenesis: false], RequestFactory)

    void setup()
    {
        converter.requestFactory = factory

        factory.request(AUTHORIZATION_REVERSAL) >> new PaymentTransaction()

        paymentInfo.billingAddress >> billingAddress
        currency.isocode >> 'USD'
        order.paymentInfo >> paymentInfo
        order.guid >> 'guid'
        order.currency >> currency
        order.totalPrice >> 25
        transactionEntry.requestId >> 'auth-request-identifier'

        source.addParam('order', order)
        source.addParam('transaction', transactionEntry)
        source.addParam('merchantId', 'merchant_identifier')
    }

    @Test
    def 'should convert source to authorization reversal request'()
    {
        when:
        def target = converter.convert(source)
        def requestFields = target.requestFields

        then:
        requestFields.apPaymentType == 'KLI'
        requestFields.apAuthReversalServiceRun == true
        requestFields.apAuthReversalServiceAuthRequestID == 'auth-request-identifier'
        requestFields.merchantId == 'merchant_identifier'
        requestFields.merchantReferenceCode == 'guid'
    }
}
