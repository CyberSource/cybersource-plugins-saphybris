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
}
