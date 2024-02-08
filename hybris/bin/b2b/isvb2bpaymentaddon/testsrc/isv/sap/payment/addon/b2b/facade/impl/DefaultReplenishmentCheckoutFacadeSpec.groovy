package isv.sap.payment.addon.b2b.facade.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.ReplenishmentInfoData
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel

@UnitTest
class DefaultReplenishmentCheckoutFacadeSpec extends Specification
{
    def cartService = Mock([useObjenesis: false], CartService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def replenishmentInfoReverseConverter = Mock([useObjenesis: false], Converter)

    def facade = new DefaultReplenishmentCheckoutFacade()

    def replenishmentModel = new ReplenishmentInfoModel()

    def replenishmentData = new ReplenishmentInfoData()

    def cart = new CartModel()

    def 'setup'()
    {
        facade.cartService = cartService
        facade.modelService = modelService
        facade.replenishmentInfoReverseConverter = replenishmentInfoReverseConverter

        replenishmentInfoReverseConverter.convert(replenishmentData) >> replenishmentModel
        cartService.sessionCart >> cart
    }

    @Test
    def 'should add replenishment'()
    {
        when:
        facade.add(replenishmentData)

        then:
        cart.replenishmentInfo == replenishmentModel
        1 * modelService.save(cart)
    }

    @Test
    def 'should remove existing replenishment and add a new one'()
    {
        given:
        cart.setReplenishmentInfo(new ReplenishmentInfoModel())

        when:
        facade.add(replenishmentData)

        then: 'should remove existing replenishment info'
        1 * modelService.remove(cart.replenishmentInfo)

        then: 'should add new replenishment info'
        cart.replenishmentInfo == replenishmentModel
        2 * modelService.save(cart)
    }

    @Test
    def 'should remove replenishment'()
    {
        given:
        cart.setReplenishmentInfo(new ReplenishmentInfoModel())

        when:
        facade.removeReplenishment()

        then:
        1 * modelService.remove(cart.replenishmentInfo)
        1 * modelService.save(cart)
    }
}
