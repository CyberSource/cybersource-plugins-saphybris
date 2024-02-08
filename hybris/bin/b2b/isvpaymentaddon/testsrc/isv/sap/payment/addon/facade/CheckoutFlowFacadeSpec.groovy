package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.CartFacade
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.order.CartService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class CheckoutFlowFacadeSpec extends Specification
{
    def cartService = Mock(CartService)

    def cartFacade = Mock(CartFacade)

    def cart = Mock(CartModel)

    def paymentInfo = Mock(PaymentInfoModel)

    def facade = new CheckoutFlowFacade()

    def setup()
    {
        facade.setCartService(cartService)
        facade.setCartFacade(cartFacade)

        cartFacade.hasSessionCart() >> true
        cartService.sessionCart >> cart
    }

    @Test
    def 'hasNoPayment should return false when cart has payment is setup'()
    {
        when:
        cart.paymentInfo >> paymentInfo

        then:
        !facade.hasNoPaymentInfo()
    }

    @Test
    def 'hasNoPayment should return ture when cart has no payment setup'()
    {
        when:
        cart.paymentInfo >> null

        then:
        facade.hasNoPaymentInfo()
    }
}
