package isv.sap.payment.addon.handler

import javax.servlet.http.HttpServletRequest

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentSource
import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.model.MerchantProfile
import isv.cjl.payment.security.service.SAService
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.request.PaymentServiceRequest

@UnitTest
class DefaultIsvResponseHandlerSpec extends Specification
{
    def merchant = Mock([useObjenesis: false], Merchant)

    def merchantProfile = Mock([useObjenesis: false], MerchantProfile)

    def order = Mock([useObjenesis: false], AbstractOrderModel)

    def sAService = Mock([useObjenesis: false], SAService)

    def merchantService = Mock([useObjenesis: false], MerchantService)

    def paymentServiceExecutor = Mock([useObjenesis: false], PaymentServiceExecutor)

    def handler = new DefaultIsvResponseHandler()

    def setup()
    {
        handler.saService = sAService
        handler.merchantService = merchantService
        handler.paymentServiceExecutor = paymentServiceExecutor

        merchantProfile.secretKey >> 'ABCD123'

        merchantService.getMerchant('merchant_1') >> merchant
        merchantService.getMerchantProfile(merchant, 'HOP') >> merchantProfile
    }

    @Test
    def 'should validate response by decision'()
    {
        when:
        def result = handler.isDecisionSuccessful([
                decision                   : decision, req_reference_number: '12345',
                req_merchant_defined_data99: 'merchant_1', req_merchant_defined_data100: 'HOP',
                reason_code                : reason_code
        ])

        then:
        result == expected

        where:
        decision | reason_code | expected
        'ACCEPT' | '100'       | true
        'REVIEW' | '100'       | true
        'REJECT' | '101'       | false
        'ERROR'  | '101'       | false
        'ERROR'  | '104'       | true
    }

    @Test
    def 'should process reponse by executing sa authorization service'()
    {
        given:
        def paymentResponse = [decision            : 'ACCEPT', req_merchant_defined_data99: 'merchant_1',
                               req_transaction_type: 'authorization']
        handler.transactionTypeMap = ['authorization': PaymentTransactionType.AUTHORIZATION]

        when:
        handler.processResponse(order, paymentResponse)

        then:
        1 * paymentServiceExecutor.execute(_) >> { arguments ->
            def PaymentServiceRequest request = arguments[0]
            assert request.paymentSource == PaymentSource.SECURE_ACCEPTANCE
            assert request.paymentType == PaymentType.CREDIT_CARD
            assert request.paymentTransactionType == PaymentTransactionType.AUTHORIZATION
            assert request.requestParams.merchantId == 'merchant_1'
            assert request.requestParams.order == order
            assert request.requestParams.paymentResponse == paymentResponse as Map
        }
    }

    @Test
    def 'should indicate if the signature is valid'()
    {
        given:
        def paymentResponse = [decision            : 'ACCEPT', req_merchant_defined_data99: 'merchant_1',
                               req_transaction_type: 'authorization', req_merchant_defined_data100: 'SOP']

        when:
        def result = handler.isValidSignature(paymentResponse)

        then:
        1 * merchantService.getMerchant('merchant_1') >> merchant
        1 * merchantService.getMerchantProfile(merchant, 'SOP') >> merchantProfile
        1 * sAService.validateTransactionSignature(paymentResponse, 'ABCD123') >> isValid
        result == expected

        where:
        isValid | expected
        true    | true
        false   | false
    }

    @Test
    def 'should get signed parameter map'()
    {
        given:
        def request = Mock([useObjenesis: false], HttpServletRequest)
        request.parameterMap >> [decision: ['ACCEPT'] as String[]]

        when:
        handler.getValidParameters(request)

        then:
        1 * sAService.getValidParameters([decision: 'ACCEPT'])
    }
}
