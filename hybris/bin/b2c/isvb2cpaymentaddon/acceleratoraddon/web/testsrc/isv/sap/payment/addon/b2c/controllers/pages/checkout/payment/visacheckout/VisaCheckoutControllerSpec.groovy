package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.visacheckout

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.facade.VisaCheckoutPaymentFacade
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade

@UnitTest
class VisaCheckoutControllerSpec extends Specification
{
    VisaCheckoutPaymentFacade paymentFacade = Mock()

    PaymentCheckoutFacade paymentCheckoutFacade = Mock()

    CartService cartService = Mock()

    CheckoutCustomerStrategy checkoutCustomerStrategy = Mock()

    VisaCheckoutController controller

    CartModel cart = new CartModel()

    OrderData order = new OrderData()

    @SuppressWarnings('BracesForClass')
    def setup()
    {
        controller = new VisaCheckoutController() {
            @Override
            protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
            {
                VisaCheckoutControllerSpec.this.checkoutCustomerStrategy
            }
        }

        controller.visaCheckoutPaymentFacade = paymentFacade
        controller.paymentCheckoutFacade = paymentCheckoutFacade
        controller.cartService = cartService

        cartService.sessionCart >> cart

        order.guid = 'oguid'
        order.code = 'ocode'
    }

    @Test
    def 'success: Should redirect to error page if callId wasnt provided'()
    {
        when:
        def redirect = controller.success(null, false)

        then:
        0 * paymentCheckoutFacade._
        redirect == AbstractController.REDIRECT_PREFIX + VisaCheckoutController.PAYMENT_ERROR_URL
    }

    @Test
    def 'success: Should redirect to error page if get vc auth return error'()
    {
        when:
        def redirect = controller.success('123456', true)

        then:
        1 * paymentFacade.authorizeVisaCheckoutPayment(_, _, _) >> false
        0 * paymentCheckoutFacade._
        redirect == AbstractController.REDIRECT_PREFIX + VisaCheckoutController.PAYMENT_ERROR_URL
    }

    @Test
    def 'success: Shoud successfully place order'(boolean isAnon, String orderId)
    {
        when:
        def redirect = controller.success('123456', true)

        then:
        1 * checkoutCustomerStrategy.isAnonymousCheckout() >> isAnon
        1 * paymentFacade.authorizeVisaCheckoutPayment(_, _, _) >> true
        1 * paymentCheckoutFacade.performPlaceOrder(cart) >> order
        redirect == AbstractController.REDIRECT_PREFIX + '/checkout/orderConfirmation/' + orderId

        where:
        isAnon || orderId
        true   || 'oguid'
        false  || 'ocode'
    }
}
