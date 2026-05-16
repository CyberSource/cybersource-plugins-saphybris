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
}
