package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.flex

import javax.servlet.http.HttpSession

import com.cybersource.flex.sdk.CaptureContext
import com.cybersource.flex.sdk.TransientToken
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.AbstractOrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.service.flex.FlexService
import isv.sap.payment.addon.facade.CreditCardPaymentFacade
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

@SuppressWarnings('BracesForClass')
@UnitTest
class FlexMicroformControllerSpec extends Specification
{
    def cartService = Mock(CartService)

    def creditCardPaymentFacade = Mock(CreditCardPaymentFacade)

    def flexService = Mock(FlexService)

    def paymentCheckoutFacade = Mock(PaymentCheckoutFacade)

    def checkoutCustomerStrategy = Mock(CheckoutCustomerStrategy)

    def controller = new FlexMicroformController() {
        @Override
        protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
        {
            FlexMicroformControllerSpec.this.checkoutCustomerStrategy
        }
    }

    def setup()
    {
        controller.cartService = cartService
        controller.creditCardPaymentFacade = creditCardPaymentFacade
        controller.flexService = flexService
        controller.paymentCheckoutFacade = paymentCheckoutFacade
    }

    @Test
    def 'Should create new json web token'()
    {
        given:
        def token = 'jwt12345'
        def uriComponentBuilder = new UriComponentsBuilder()
        def httpSession = Mock(HttpSession)
        def captureContext = Mock(CaptureContext)
        captureContext.toString() >> token

        when:
        def actualToken = controller.newJwk(httpSession, uriComponentBuilder)

        then:
        1 * flexService.createKey(_ as String) >> captureContext
        1 * httpSession.setAttribute('captureContext', token)
        actualToken == token
    }

    @Test
    @Unroll
    def 'Should verify existing token'()
    {
        given:
        def token = 'jwt12345'
        def captureContext = 'captureContext'
        def httpSession = Mock(HttpSession)
        httpSession.getAttribute('captureContext') >> captureContext

        when:
        def response = controller.verifyToken(token, httpSession)

        then:
        1 * flexService.verifyAndGet(captureContext, token) >> transientTokenParam
        response.statusCode == statusCodeParam

        where:
        transientTokenParam               | statusCodeParam
        Optional.of(Stub(TransientToken)) | HttpStatus.OK
        Optional.empty()                  | HttpStatus.UNPROCESSABLE_ENTITY
    }

    @Test
    def 'Should process payment without 3ds'()
    {
        given:
        def transientToken = 'jwt12345'
        creditCardPaymentFacade.is3dsEnabled() >> false
        checkoutCustomerStrategy.isAnonymousCheckout() >> false

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        def orderData = new AbstractOrderData()
        orderData.code = 'Order12345'

        when:
        def redirectUrl = controller.pay(transientToken)

        then:
        1 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(cartModel, transientToken) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cartModel) >> orderData
        redirectUrl == "redirect:/checkout/orderConfirmation/${orderData.code}"
    }

    @Test
    def 'Should not process payment if 3ds is enabled'()
    {
        given:
        def transientToken = 'jwt12345'
        creditCardPaymentFacade.is3dsEnabled() >> true

        when:
        def redirectUrl = controller.pay(transientToken)

        then:
        0 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)
        redirectUrl == 'redirect:/checkout/multi/summary/view/payment/error'
    }

    @Test
    def 'Should process payment without validation'()
    {
        given:
        def transientToken = 'jwt12345'
        def referenceId = 'ref12345'
        def enrollmentTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTransaction.properties >> ['payerAuthEnrollReplyReasonCode': '100']

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        def orderData = new AbstractOrderData()
        orderData.guid = 'Order12345'
        checkoutCustomerStrategy.isAnonymousCheckout() >> true

        when:
        def response = controller.payWithoutValidation(referenceId, transientToken)

        then:
        1 * creditCardPaymentFacade.enrollCreditCard(referenceId, transientToken) >> enrollmentTransaction
        1 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(cartModel, transientToken, enrollmentTransaction) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cartModel) >> orderData

        and:
        response.success
        response.data['responseCode'] == '100'
        response.data['redirectUrl'] == "/checkout/orderConfirmation/${orderData.guid}"
    }

    @Test
    def 'Should process payment without validation and no auth enrollment'()
    {
        given:
        def transientToken = 'jwt12345'
        def referenceId = 'ref12345'
        def notEnrolledCode = '100'
        def enrollmentTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTransaction.properties >> ['payerAuthEnrollReplyReasonCode': notEnrolledCode]

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        def orderData = new AbstractOrderData()
        orderData.guid = 'Order12345'
        checkoutCustomerStrategy.isAnonymousCheckout() >> true

        when:
        def response = controller.payWithoutValidation(referenceId, transientToken)

        then:
        1 * creditCardPaymentFacade.enrollCreditCard(referenceId, transientToken) >> enrollmentTransaction
        1 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(cartModel, transientToken, enrollmentTransaction) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cartModel) >> orderData

        and:
        response.success
        response.data['responseCode'] == notEnrolledCode
        response.data['redirectUrl'] == "/checkout/orderConfirmation/${orderData.guid}"
    }

    @Test
    def 'Should process payment without validation and auth enrollment'()
    {
        given:
        def transientToken = 'jwt12345'
        def referenceId = 'ref12345'
        def enrolledCode = '475'
        def enrollmentTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTransaction.properties >> [
                'payerAuthEnrollReplyReasonCode'                 : enrolledCode,
                'payerAuthEnrollReplyAuthenticationTransactionID': 'transactionId12345',
                'payerAuthEnrollReplyAcsURL'                     : 'enroll/url/test/'
        ]

        when:
        def response = controller.payWithoutValidation(referenceId, transientToken)

        then:
        1 * creditCardPaymentFacade.enrollCreditCard(referenceId, transientToken) >> enrollmentTransaction

        and:
        response.success
        response.data['responseCode'] == enrolledCode
        response.data['transactionId'] == 'transactionId12345'
        response.data['acsUrl'] == 'enroll/url/test/'
    }

    @Test
    def 'Should fail to process payment without validation if no enrollment specified'()
    {
        given:
        def transientToken = 'jwt12345'
        def referenceId = 'ref12345'
        def enrollmentTransaction = Mock(IsvPaymentTransactionEntryModel)
        enrollmentTransaction.properties >> [:]

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        cartModel.code >> 'code12345'

        when:
        def response = controller.payWithoutValidation(referenceId, transientToken)

        then:
        1 * creditCardPaymentFacade.enrollCreditCard(referenceId, transientToken) >> enrollmentTransaction

        and:
        !response.success
        response.data['redirectUrl'] == '/checkout/multi/summary/view/payment/error'
    }

    @Test
    def 'Should fail to process payment is an exception occurs'()
    {
        given:
        def transientToken = 'jwt12345'
        def referenceId = 'ref12345'
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        cartModel.code >> 'code12345'

        and:
        creditCardPaymentFacade.enrollCreditCard(referenceId, transientToken) >> {
            throw new Exception('Could not create enrollment transaction')
        }

        when:
        def response = controller.payWithoutValidation(referenceId, transientToken)

        then:
        !response.success
        response.data['redirectUrl'] == '/checkout/multi/summary/view/payment/error'
    }

    @Test
    def 'Should process payment with validation'()
    {
        given:
        def transientToken = 'token12345'
        def authJwt = 'jwt12345'

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        def orderData = new AbstractOrderData()
        orderData.code = 'Order12345'
        checkoutCustomerStrategy.isAnonymousCheckout() >> false

        when:
        def response = controller.pay(transientToken, authJwt)

        then:
        1 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(cartModel, transientToken, authJwt) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cartModel) >> orderData

        and:
        response.data['redirectUrl'] == "/checkout/orderConfirmation/${orderData.code}"
    }

    @Test
    def 'Should fail to process payment with validation if an exception occurs'()
    {
        given:
        def transientToken = 'token12345'
        def authJwt = 'jwt12345'

        and:
        def cartModel = Mock(CartModel)
        cartService.sessionCart >> cartModel
        def orderData = new AbstractOrderData()
        orderData.code = 'Order12345'
        checkoutCustomerStrategy.isAnonymousCheckout() >> false

        when:
        def response = controller.pay(transientToken, authJwt)

        then:
        1 * creditCardPaymentFacade.authorizeFlexCreditCardPayment(cartModel, transientToken, authJwt) >> {
            throw new Exception('Failed to authorize credit card payment')
        }

        and:
        0 * paymentCheckoutFacade.performPlaceOrder(_)
        response.data['redirectUrl'] == '/checkout/multi/summary/view/payment/error'
    }
}
