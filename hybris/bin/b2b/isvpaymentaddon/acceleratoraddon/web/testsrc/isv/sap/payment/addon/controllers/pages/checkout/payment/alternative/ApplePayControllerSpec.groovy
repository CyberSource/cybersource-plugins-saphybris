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
    def 'Should validate ApplePay merchant'()
    {
        when:
        controller.validateMerchant('validationUrl')

        then:
        1 * applePayPaymentFacade.createApplePaySession('validationUrl')
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
