package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.AbstractOrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import isv.sap.payment.addon.facade.GooglePayPaymentFacade
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade

import static isv.sap.payment.addon.controllers.pages.checkout.payment.alternative.GooglePayController.PAYMENT_ERROR_URL

@UnitTest
class GooglePayControllerSpec extends Specification
{
    GooglePayPaymentFacade googlePayPaymentFacade = Mock()

    CartService cartService = Mock()

    PaymentCheckoutFacade paymentCheckoutFacade = Mock()

    CheckoutCustomerStrategy checkoutCustomerStrategy = Mock()

    @SuppressWarnings('BracesForClass')
    def controller = new GooglePayController() {
        @Override
        protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
        {
            GooglePayControllerSpec.this.checkoutCustomerStrategy
        }
    }

    Map paymentData = Mock()

    CartModel cart = Mock()

    def setup()
    {
        cartService.sessionCart >> cart
        controller.googlePayPaymentFacade = googlePayPaymentFacade
        controller.cartService = cartService
        controller.paymentCheckoutFacade = paymentCheckoutFacade
    }

    @Test
    def 'placeOrder: should redirect to error page if payment not authorized'()
    {
        when:
        def response = controller.placeOrder(paymentData)

        then:
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> false
        0 * paymentCheckoutFacade.performPlaceOrder(cart)
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should redirect to error page if error happens'()
    {
        when:
        def response = controller.placeOrder(paymentData)
        then:
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> { throw new RuntimeException() }
        0 * paymentCheckoutFacade.performPlaceOrder(cart)
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should place order and redirect to confirmation page if authorization is successful'()
    {
        given:
        AbstractOrderData orderData = Mock()
        orderData.guid >> 'guid'

        when:
        def response = controller.placeOrder(paymentData)

        then:
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> orderData
        1 * checkoutCustomerStrategy.isAnonymousCheckout() >> true
        response == ResponseEntity.ok('/checkout/orderConfirmation/guid')
    }

    // Negative tests for cart integrity validation (race condition protection)

    @Test
    def 'placeOrder: should reject order placement when cart validation fails after authorization'()
    {
        when:
        def response = controller.placeOrder(paymentData)

        then:
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true
        1 * paymentCheckoutFacade.validateCart() >> false
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should reject when cart state changes between authorization and placement'()
    {
        given: 'Cart has been authorized'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def response = controller.placeOrder(paymentData)

        then: 'Authorization succeeds'
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true

        and: 'Cart validation fails due to state change'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is never attempted'
        0 * paymentCartService.executeWithCartLock(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Error response is returned'
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should handle exception during order placement within cart lock'()
    {
        given: 'Cart has been authorized and validated'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def response = controller.placeOrder(paymentData)

        then: 'Authorization succeeds'
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true

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
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should validate cart before acquiring lock to prevent unnecessary locking'()
    {
        given: 'Cart validation will fail'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def response = controller.placeOrder(paymentData)

        then: 'Authorization succeeds'
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true

        and: 'Cart validation fails first (before lock attempt)'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Cart lock is never acquired'
        0 * paymentCartService.executeWithCartLock(_, _)

        and: 'Order placement is never attempted'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should reject order when cart validation detects concurrent modification'()
    {
        given: 'Simulating race condition scenario'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Multiple concurrent requests attempt to place order'
        def response = controller.placeOrder(paymentData)

        then: 'First request authorizes successfully'
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true

        and: 'Cart validation detects that cart was modified by concurrent request'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Order placement is blocked to prevent double-charge'
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Error is returned'
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should return error when order placement returns null'()
    {
        given: 'Cart validation succeeds but order placement returns null'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when:
        def response = controller.placeOrder(paymentData)

        then: 'Authorization succeeds'
        1 * googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, cart) >> true

        and: 'Cart validation succeeds'
        1 * paymentCheckoutFacade.validateCart() >> true

        and: 'Order placement returns null within cart lock'
        1 * paymentCartService.executeWithCartLock(cart, _) >> { CartModel c, Runnable body ->
            body.run()
        }
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> null

        and: 'Error response is returned'
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }
}
