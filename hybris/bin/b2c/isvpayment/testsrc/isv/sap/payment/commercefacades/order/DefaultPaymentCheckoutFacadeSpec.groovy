package isv.sap.payment.commercefacades.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification

@UnitTest
class DefaultPaymentCheckoutFacadeSpec extends Specification
{
    @Shared
    def address = Mock([useObjenesis: false], AddressModel)

    @Shared
    def deliveryMode = Mock([useObjenesis: false], DeliveryModeModel)

    @Shared
    def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

    def cart = Mock([useObjenesis: false], CartModel)

    def order = Mock([useObjenesis: false], OrderModel)

    def orderData = Mock([useObjenesis: false], OrderData)

    def orderConverter = Mock([useObjenesis: false], Converter)

    def cartService = Mock([useObjenesis: false], CartService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def facade = Spy(DefaultPaymentCheckoutFacade)

    def setup()
    {
        facade.orderConverter = orderConverter
        facade.cartService = cartService
        facade.modelService = modelService

        orderConverter.convert(order) >> orderData
    }

    @Test
    def 'should validate data for specified cart model'()
    {
        given:
        cart.deliveryAddress >> deliveryAddress
        cart.deliveryMode >> mode
        cart.paymentInfo >> payment

        when:
        def result = facade.validOrder(cart)

        then:
        result == expectedResult

        where:
        deliveryAddress | mode         | payment     | expectedResult
        address         | deliveryMode | paymentInfo | true
        null            | deliveryMode | paymentInfo | false
        address         | null         | paymentInfo | false
        address         | deliveryMode | null        | false
    }

    @Test
    def 'should place order and return order data object'()
    {
        when:
        def resultData = facade.performPlaceOrder(cart)

        then:
        1 * facade.beforePlaceOrder(cart) >> { }
        1 * facade.placeOrder(cart) >> order
        1 * facade.afterPlaceOrder(cart, _ as OrderModel) >> { }
        resultData == orderData
    }

    @Test
    def 'should return null if cart model not provided'()
    {
        when:
        def resultData = facade.performPlaceOrder(null)

        then:
        resultData == null
    }

    @Test
    def 'should remove given cart and refresh order model'()
    {
        given:
        cartService.hasSessionCart() >> false

        when:
        facade.afterPlaceOrder(cart, order)

        then:
        1 * modelService.remove(cart)
        1 * modelService.refresh(order)
    }

    @Test
    def 'should remove session cart and refresh order model'()
    {
        given:
        cartService.hasSessionCart() >> true

        when:
        facade.afterPlaceOrder(cart, order)

        then:
        1 * cartService.removeSessionCart()
        1 * modelService.refresh(order)
    }

    @Test
    def 'should do nothing if order is null'()
    {
        when:
        facade.afterPlaceOrder(cart, null)

        then:
        0 * cartService._
        0 * modelService._
    }
}
