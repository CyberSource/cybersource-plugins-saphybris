package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.klarna

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.constants.IsvPaymentAddonConstants
import isv.sap.payment.addon.facade.KlarnaPaymentFacade

@UnitTest
class KlarnaControllerSpec extends Specification
{
    def controller = new KlarnaController()

    def paymentFacade = Mock(KlarnaPaymentFacade)

    def cartService = Mock(CartService)

    def cart = new CartModel()

    def setup()
    {
        controller.klarnaPaymentFacade = paymentFacade
        controller.cartService = cartService
        cartService.sessionCart >> cart
    }

    @Test
    def 'createSession: should return klarna session id'()
    {
        when:
        def result = controller.createSession()

        then:
        1 * paymentFacade.createKlarnaSession(cart) >> 'xxxyyy111'
        result.success
        result.data[IsvPaymentAddonConstants.AlternativePayments.KLARNA_SESSION_ID] == 'xxxyyy111'
    }

    @Test
    def 'createSession: should return no success in case of exception'()
    {
        when:
        def result = controller.createSession()

        then:
        1 * paymentFacade.createKlarnaSession(cart) >> { throw new IllegalStateException() }
        !result.success
        result.data == [:]
    }

    @Test
    def 'updateSession: should return klarna session id'()
    {
        when:
        def result = controller.updateSession()

        then:
        1 * paymentFacade.updateKlarnaSession(cart) >> 'xxxyyy111'
        result.success
        result.data[IsvPaymentAddonConstants.AlternativePayments.KLARNA_SESSION_ID] == 'xxxyyy111'
    }

    @Test
    def 'updateSession: should return no success in case of exception'()
    {
        when:
        def result = controller.updateSession()

        then:
        1 * paymentFacade.updateKlarnaSession(cart) >> { throw new IllegalStateException() }
        !result.success
        result.data == [:]
    }
}
