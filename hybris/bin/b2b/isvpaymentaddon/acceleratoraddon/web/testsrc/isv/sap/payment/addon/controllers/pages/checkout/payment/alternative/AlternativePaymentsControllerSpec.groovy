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
}
