package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import org.springframework.ui.Model
import spock.lang.Specification
import spock.lang.Unroll

import isv.sap.payment.addon.constants.IsvPaymentAddonConstants
import isv.sap.payment.addon.facade.AlternativePaymentFacade
import isv.sap.payment.addon.facade.AlternativePaymentStatusFacade
import isv.sap.payment.addon.facade.AlternativePaymentStatusFacadeImpl
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade

import static de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController.REDIRECT_PREFIX
import static isv.sap.payment.addon.controllers.pages.checkout.payment.alternative.AlternativePaymentsController.PAYMENT_FAILED
import static isv.sap.payment.addon.controllers.pages.checkout.payment.alternative.AlternativePaymentsController.PLACE_ORDER_PAYMENT_ERROR
import static isv.sap.payment.addon.enums.CheckStatusResponse.PAYMENT_PENDING
import static isv.sap.payment.addon.enums.CheckStatusResponse.PAYMENT_SUCCESS
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK

@UnitTest
class AlternativePaymentsControllerSpec extends Specification
{
    AlternativePaymentsController controller

    CartService cartService = Mock(CartService)

    PaymentCheckoutFacade paymentCheckoutFacade = Mock(PaymentCheckoutFacade)

    AlternativePaymentFacade paymentFacade = Mock(AlternativePaymentFacade)

    CheckoutCustomerStrategy checkoutStrategy = Mock(CheckoutCustomerStrategy)

    AlternativePaymentStatusFacade alternativePaymentStatusFacade = Mock(AlternativePaymentStatusFacadeImpl)

    Model model = Mock(Model)

    CartModel cart = new CartModel()

    @SuppressWarnings('BracesForClass')
    def setup()
    {
        controller = new AlternativePaymentsController() {
            @Override
            protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
            {
                checkoutStrategy
            }
        }
        controller.cartService = cartService
        controller.paymentCheckoutFacade = paymentCheckoutFacade
        controller.alternativePaymentFacade = paymentFacade
        controller.alternativePaymentStatusFacade = alternativePaymentStatusFacade

        cartService.sessionCart >> cart
    }

    @Test
    def 'Should return an entity containing the redirect to payment failed if ap payment call throws exception'()
    {
        when:
        def response = controller.payNoRedirect('ideal', null, null, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(*_) >> { throw new RuntimeException() }
        1 * model.asMap() >> [:]
        1 * model.addAttribute('accErrorMsgs') {
            it.element.code == PLACE_ORDER_PAYMENT_ERROR
        }
        response.status == BAD_REQUEST
        response.body == '/checkout/multi/summary/view/payment/error'
    }

    @Test
    def 'Should return an entity containing the redirect to payment failed if ap payment call failed'()
    {
        when:
        def response = controller.payNoRedirect('ideal', null, null, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(*_) >> Optional.empty()
        1 * model.asMap() >> [:]
        1 * model.addAttribute('accErrorMsgs') {
            it.element.code == PLACE_ORDER_PAYMENT_ERROR
        }
        response.status == BAD_REQUEST
        response.body == '/checkout/multi/summary/view/payment/error'
    }

    @Test
    def 'Should return an entity containing the redirect to thridparty payment gateway if ap payment call was successful'()
    {
        when:
        def response = controller.payNoRedirect(paymentModeCode, paymentOptionId, klarnaAuthToken, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(cart, paymentModeCode,
                                                               [(IsvPaymentAddonConstants.AlternativePayments.PAYMENT_OPTION_ID): paymentOptionId,
                 (IsvPaymentAddonConstants.AlternativePayments.KLARNA_AUTH_TOKEN): klarnaAuthToken]) >> Optional.of('www.gateway.com')

        response.statusCode == OK
        response.body == 'www.gateway.com'

        where:
        paymentModeCode | paymentOptionId | klarnaAuthToken
        'ideal'         | null            | null
        'ideal'         | 'option1'       | null
        'kli'           | null            | '999yyy'
    }

    @Test
    def 'Should redirect to payment failed if ap payment call throws exception'()
    {
        when:
        def redirect = controller.pay('ideal', null, null, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(*_) >> { throw new RuntimeException() }
        1 * model.asMap() >> [:]
        1 * model.addAttribute('accErrorMsgs') {
            it.element.code == PLACE_ORDER_PAYMENT_ERROR
        }
        redirect == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'Should redirect to payment failed if ap payment call failed'()
    {
        when:
        def redirect = controller.pay('ideal', null, null, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(*_) >> Optional.empty()
        1 * model.asMap() >> [:]
        1 * model.addAttribute('accErrorMsgs') {
            it.element.code == PLACE_ORDER_PAYMENT_ERROR
        }
        redirect == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'Should redirect to thridparty payment gateway if ap payment call was successful'()
    {
        when:
        def redirect = controller.pay(paymentModeCode, paymentOptionId, klarnaAuthToken, model)

        then:
        1 * paymentFacade.makeSaleRequestForAlternativePayment(cart, paymentModeCode,
                                                               [(IsvPaymentAddonConstants.AlternativePayments.PAYMENT_OPTION_ID): paymentOptionId,
                 (IsvPaymentAddonConstants.AlternativePayments.KLARNA_AUTH_TOKEN): klarnaAuthToken]) >> Optional.of('www.gateway.com')
        redirect == REDIRECT_PREFIX + 'www.gateway.com'

        where:
        paymentModeCode | paymentOptionId | klarnaAuthToken
        'ideal'         | null            | null
        'ideal'         | 'option1'       | null
        'kli'           | null            | '999yyy'
    }

    @Test
    def 'should redirect to payment failed if ap validation throws exception'()
    {
        when:
        def res = controller.handleReturn('sof')

        then:
        1 * paymentFacade.validateAlternativePaymentResponse(_, _) >> { throw new RuntimeException() }
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'should redirect to payment failed if ap validation wasnt successful'()
    {
        when:
        def res = controller.handleReturn('sof')

        then:
        1 * paymentFacade.validateAlternativePaymentResponse(_, _) >> false
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'should redirect to orderConfirmation if ap validation was successful'()
    {
        given:
        CartModel cart = new CartModel()
        OrderData order = new OrderData()
        order.guid = '12345'

        when:
        def res = controller.handleReturn('sof')

        then:
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'sof') >> true
        1 * cartService.sessionCart >> cart
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> order
        1 * checkoutStrategy.isAnonymousCheckout() >> true
        res == REDIRECT_PREFIX + '/checkout/orderConfirmation/12345?ap=sof'
    }

    @Test
    @Unroll
    def 'Should check order status and return and entity containing the payment status'()
    {
        when:
        def response = controller.isOrderPlaced()

        then:
        1 * cartService.sessionCart >> cart
        1 * alternativePaymentStatusFacade.resolveCheckStatusResponse(cart) >> checkStatusResponse

        response.status == OK
        response.body == responseString

        where:
        checkStatusResponse            || responseString
        PAYMENT_SUCCESS                || PAYMENT_SUCCESS
        PAYMENT_PENDING                || PAYMENT_PENDING
    }

    // Negative tests for cart integrity validation (race condition protection)

    @Test
    def 'handleReturn: should reject order placement when cart validation fails after authorization'()
    {
        given:
        CartModel cart = new CartModel()

        when:
        def res = controller.handleReturn('sof')

        then:
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'sof') >> true
        1 * paymentCheckoutFacade.validateCart() >> false
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should reject when cart state changes between authorization and placement'()
    {
        given: 'Cart has been authorized'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def res = controller.handleReturn('ideal')

        then: 'Authorization succeeds'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'ideal') >> true

        and: 'Cart validation fails due to state change'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is never attempted'
        0 * paymentCartService.executeWithCartLock(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Error response is returned'
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should handle exception during order placement within cart lock'()
    {
        given: 'Cart has been authorized and validated'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def res = controller.handleReturn('wechat')

        then: 'Authorization succeeds'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'wechat') >> true

        and: 'Cart validation succeeds'
        1 * paymentCheckoutFacade.validateCart() >> true

        and: 'Exception occurs during order placement'
        1 * paymentCartService.executeWithCartLock(cart, _) >> { CartModel c, Runnable body ->
            body.run()
        }
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> {
            throw new RuntimeException('Order placement failed due to cart state change')
        }

        and: 'Error response is returned'
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should validate cart before acquiring lock to prevent unnecessary locking'()
    {
        given: 'Cart validation will fail'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def res = controller.handleReturn('bancontact')

        then: 'Authorization succeeds'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'bancontact') >> true

        and: 'Cart validation fails first (before lock attempt)'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Cart lock is never acquired'
        0 * paymentCartService.executeWithCartLock(_, _)

        and: 'Order placement is never attempted'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should reject order when cart validation detects concurrent modification'()
    {
        given: 'Simulating race condition scenario'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Multiple concurrent requests attempt to place order'
        def res = controller.handleReturn('sofort')

        then: 'First request authorizes successfully'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'sofort') >> true

        and: 'Cart validation detects that cart was modified by concurrent request'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is blocked to prevent double-charge'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Error is returned'
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should return error when order placement returns null'()
    {
        given: 'Cart validation succeeds but order placement returns null'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def res = controller.handleReturn('alipay')

        then: 'Authorization succeeds'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'alipay') >> true

        and: 'Cart validation succeeds'
        1 * paymentCheckoutFacade.validateCart() >> true

        and: 'Order placement returns null within cart lock'
        1 * paymentCartService.executeWithCartLock(cart, _) >> { CartModel c, Runnable body ->
            body.run()
        }
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> null

        and: 'Error response is returned'
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should protect against TOCTOU race condition attack'()
    {
        given: 'Attacker attempts to modify cart between authorization and placement'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Attacker triggers order placement with compromised cart'
        def res = controller.handleReturn('klarna')

        then: 'Authorization succeeds with original cart state'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, 'klarna') >> true

        and: 'Cart validation detects the attack (cart was modified after authorization)'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Attack is blocked - no order is created'
        0 * paymentCartService.executeWithCartLock(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Attacker receives error response'
        res == REDIRECT_PREFIX + PAYMENT_FAILED
    }

    @Test
    def 'handleReturn: should prevent race condition with multiple payment methods'()
    {
        given: 'Testing race condition protection across different alternative payment methods'
        CartModel cart = new CartModel()
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Request for iDEAL payment with cart modified after authorization'
        def res = controller.handleReturn(paymentMethod)

        then: 'Payment validation succeeds'
        1 * cartService.sessionCart >> cart
        1 * paymentFacade.validateAlternativePaymentResponse(cart, paymentMethod) >> true

        and: 'Cart validation detects state change'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is blocked'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'User redirected to error page'
        res == REDIRECT_PREFIX + PAYMENT_FAILED

        where:
        paymentMethod << ['ideal', 'sofort', 'bancontact', 'wechat', 'alipay', 'klarna', 'kli']
    }
}
