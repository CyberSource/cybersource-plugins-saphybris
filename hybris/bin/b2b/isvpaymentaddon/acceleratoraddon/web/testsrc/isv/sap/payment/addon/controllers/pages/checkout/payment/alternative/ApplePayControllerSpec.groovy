package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.AbstractOrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import isv.sap.payment.addon.facade.ApplePayPaymentFacade
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade

import static isv.sap.payment.addon.controllers.pages.checkout.payment.alternative.ApplePayController.PAYMENT_ERROR_URL

@UnitTest
class ApplePayControllerSpec extends Specification
{
    ApplePayPaymentFacade applePayPaymentFacade = Mock()

    CartService cartService = Mock()

    PaymentCheckoutFacade paymentCheckoutFacade = Mock()

    CheckoutCustomerStrategy checkoutCustomerStrategy = Mock()

    @SuppressWarnings('BracesForClass')
    def controller = new ApplePayController() {
        @Override
        protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
        {
            ApplePayControllerSpec.this.checkoutCustomerStrategy
        }
    }

    Map paymentToken = Mock()

    CartModel cart = Mock()

    def setup()
    {
        cartService.sessionCart >> cart
        controller.applePayPaymentFacade = applePayPaymentFacade
        controller.cartService = cartService
        controller.paymentCheckoutFacade = paymentCheckoutFacade
    }

    @Test
    def 'Should validate ApplePay merchant with valid Apple URL'()
    {
        given:
        def validUrl = 'https://apple-pay-gateway.apple.com/paymentservices/startSession'

        when:
        def response = controller.validateMerchant(validUrl)

        then:
        1 * applePayPaymentFacade.createApplePaySession(validUrl)
        response.statusCode.value() == 200
    }

    @Test
    def 'Should reject validation with non-Apple domain'()
    {
        given:
        def maliciousUrl = 'https://evil.com/paymentservices/startSession'

        when:
        def response = controller.validateMerchant(maliciousUrl)

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 403
    }

    @Test
    def 'Should reject validation with non-HTTPS URL'()
    {
        given:
        def insecureUrl = 'http://apple-pay-gateway.apple.com/paymentservices/startSession'

        when:
        def response = controller.validateMerchant(insecureUrl)

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 400
    }

    @Test
    def 'Should reject validation with empty URL'()
    {
        when:
        def response = controller.validateMerchant('')

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 400
    }

    @Test
    def 'Should reject validation with null URL'()
    {
        when:
        def response = controller.validateMerchant(null)

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 400
    }

    @Test
    def 'Should reject validation with malformed URL'()
    {
        when:
        def response = controller.validateMerchant('not-a-valid-url')

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 400
    }

    @Test
    def 'Should reject validation with credentials in URL'()
    {
        given:
        def urlWithCredentials = 'https://user:pass@apple-pay-gateway.apple.com/paymentservices/startSession'

        when:
        def response = controller.validateMerchant(urlWithCredentials)

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 400
    }

    @Test
    def 'Should validate with all legitimate Apple Pay gateway domains'()
    {
        given:
        def validUrls = [
                'https://apple-pay-gateway.apple.com/paymentservices/startSession',
                'https://cn-apple-pay-gateway.apple.com/paymentservices/startSession',
                'https://apple-pay-gateway-cert.apple.com/paymentservices/startSession',
                'https://cn-apple-pay-gateway-cert.apple.com/paymentservices/startSession'
        ]

        when:
        def responses = validUrls.collect { controller.validateMerchant(it) }

        then:
        validUrls.size() * applePayPaymentFacade.createApplePaySession(_)
        responses.every { it.statusCode.value() == 200 }
    }

    @Test
    def 'Should reject validation with subdomain that is not in allowlist'()
    {
        given:
        def unauthorizedUrl = 'https://apple-pay-gateway-nc-pod1.apple.com/paymentservices/startSession'

        when:
        def response = controller.validateMerchant(unauthorizedUrl)

        then:
        0 * applePayPaymentFacade.createApplePaySession(_)
        response.statusCode.value() == 403
    }

    @Test
    def 'placeOrder: should redirect to error page if payment not authorized'()
    {
        when:
        def response = controller.placeOrder(paymentToken)

        then:
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> false
        0 * paymentCheckoutFacade.performPlaceOrder(cart)
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }

    @Test
    def 'placeOrder: should redirect to error page if error happens'()
    {
        when:
        def response = controller.placeOrder(paymentToken)
        then:
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> { throw new RuntimeException() }
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
        def response = controller.placeOrder(paymentToken)

        then:
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> orderData
        1 * checkoutCustomerStrategy.isAnonymousCheckout() >> true
        response == ResponseEntity.ok('/checkout/orderConfirmation/guid')
    }

    // Negative tests for cart integrity validation (race condition protection)

    @Test
    def 'placeOrder: should reject order placement when cart validation fails after authorization'()
    {
        when:
        def response = controller.placeOrder(paymentToken)

        then:
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true
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
        def response = controller.placeOrder(paymentToken)

        then: 'Authorization succeeds'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

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
        def response = controller.placeOrder(paymentToken)

        then: 'Authorization succeeds'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

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
        def response = controller.placeOrder(paymentToken)

        then: 'Authorization succeeds'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

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
        def response = controller.placeOrder(paymentToken)

        then: 'First request authorizes successfully'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

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
        def response = controller.placeOrder(paymentToken)

        then: 'Authorization succeeds'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

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

    @Test
    def 'placeOrder: should protect against TOCTOU race condition attack'()
    {
        given: 'Attacker attempts to modify cart between authorization and placement'
        def paymentCartService = Mock(isv.sap.payment.commerceservices.order.PaymentCartService)
        controller.paymentCartService = paymentCartService

        when: 'Attacker triggers order placement with compromised cart'
        def response = controller.placeOrder(paymentToken)

        then: 'Authorization succeeds with original cart state'
        1 * applePayPaymentFacade.authorizeApplePayPayment(paymentToken, cart) >> true

        and: 'Cart validation detects the attack (cart was modified after authorization)'
        1 * paymentCheckoutFacade.validateCart() >> false

        and: 'Attack is blocked - no order is created'
        0 * paymentCartService.executeWithCartLock(_, _)
        0 * paymentCheckoutFacade.performPlaceOrder(_)

        and: 'Attacker receives error response'
        response == ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL)
    }
}
