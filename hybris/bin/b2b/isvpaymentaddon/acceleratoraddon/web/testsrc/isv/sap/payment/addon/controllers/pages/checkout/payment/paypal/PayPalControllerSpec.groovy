package isv.sap.payment.addon.controllers.pages.checkout.payment.paypal

import com.google.common.collect.Maps
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import org.springframework.ui.Model
import spock.lang.Specification

import isv.sap.payment.addon.facade.PayPalPaymentFacade
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade

import static de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController.REDIRECT_PREFIX

@SuppressWarnings('BracesForClass')
@UnitTest
class PayPalControllerSpec extends Specification
{
    def cartService = Mock(CartService)

    def paymentCheckoutFacade = Mock(PaymentCheckoutFacade)

    def paymentFacade = Mock(PayPalPaymentFacade)

    def checkoutCustomerStrategy = Mock(CheckoutCustomerStrategy)

    def model = Mock(Model)

    def cart = new CartModel()

    def controller = new PayPalController() {
        @Override
        protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
        {
            PayPalControllerSpec.this.checkoutCustomerStrategy
        }
    }

    def setup()
    {
        model.asMap() >> Maps.newHashMap()

        cartService.sessionCart >> cart

        controller.cartService = cartService
        controller.payPalPaymentFacade = paymentFacade
        controller.paymentCheckoutFacade = paymentCheckoutFacade
    }

    @Test
    def 'Should redirect to payment error page because of runtime exception'()
    {
        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then:
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> { throw new IllegalArgumentException('smt. bad') }
        0 * paymentCheckoutFacade._
        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should redirect to payment error page because of failed call to paypal authorization service'()
    {
        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then:
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> false
        0 * paymentCheckoutFacade._
        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should place order and redirect customer to "thank you" page'()
    {
        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then:
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> orderData
        1 * checkoutCustomerStrategy.isAnonymousCheckout() >> isAnon

        view == successURL

        where:
        orderData                   | isAnon || successURL
        new OrderData(guid: 'guid') | true   || REDIRECT_PREFIX + '/checkout/orderConfirmation/guid'
        new OrderData(code: 'code') | false  || REDIRECT_PREFIX + '/checkout/orderConfirmation/code'
    }

    @Test
    def 'Should execute the PayPal Set operation and redirect to PayPal SandBox'()
    {
        when:
        def result = controller.startFlow(model)

        then:
        result == 'redirect:EC-000111'
        1 * paymentFacade.executePayPalExpressCheckoutCreateSessionRequest(cart) >> 'EC-000111'
    }

    @Test
    def 'When PayPal SET fails go to a error page'()
    {
        when:
        def result = controller.startFlow(model)

        then:
        result == 'redirect:/checkout/multi/summary/view/payment/error'
        1 * paymentFacade.executePayPalExpressCheckoutCreateSessionRequest(cart) >> { throw new RuntimeException('exception') }
    }

    // Negative tests for cart integrity validation (race condition protection)

    @Test
    def 'Should reject order placement when cart validation fails after authorization'()
    {
        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then:
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> true
        1 * paymentCheckoutFacade.validateCart() >> false
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should reject order placement when cart state changes between authorization and placement'()
    {
        given: 'Cart has been authorized'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then: 'Authorization succeeds'
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> true

        and: 'Cart validation fails due to state change'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is never attempted'
        0 * paymentCartService.executeWithCartLock(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'User is redirected to error page'
        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should handle exception during order placement within cart lock'()
    {
        given: 'Cart has been authorized and validated'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then: 'Authorization succeeds'
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> true

        and: 'Cart validation succeeds'
        1 * paymentCheckoutFacade.validateCart() >> true

        and: 'Exception occurs during order placement'
        1 * paymentCartService.executeWithCartLock(cart, _) >> { CartModel c, Runnable body ->
            body.run()
        }
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> {
            throw new RuntimeException('Order placement failed due to cart state change')
        }

        and: 'User is redirected to error page'
        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should validate cart before acquiring lock to prevent unnecessary locking'()
    {
        given: 'Cart validation will fail'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        String view = controller.handleResponse('xxx', 'yyy')

        then: 'Authorization succeeds'
        1 * paymentFacade.authorizePayPalPayment(cart, 'xxx') >> true

        and: 'Cart validation fails first (before lock attempt)'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Cart lock is never acquired'
        0 * paymentCartService.executeWithCartLock(_, _)

        and: 'Order placement is never attempted'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }

    @Test
    def 'Should reject order when cart validation detects concurrent modification'()
    {
        given: 'Simulating race condition scenario'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Multiple concurrent requests attempt to place order'
        String view = controller.handleResponse('token123', 'payer456')

        then: 'First request authorizes successfully'
        1 * paymentFacade.authorizePayPalPayment(cart, 'token123') >> true

        and: 'Cart validation detects that cart was modified by concurrent request'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is blocked to prevent double-charge'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Error is returned'
        view == REDIRECT_PREFIX + PayPalController.PAYMENT_ERROR_URL
    }
}
