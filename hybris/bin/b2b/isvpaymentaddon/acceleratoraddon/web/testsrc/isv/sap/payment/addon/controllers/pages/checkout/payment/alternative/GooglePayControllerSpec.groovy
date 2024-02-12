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
}
