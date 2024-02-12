package isv.sap.payment.addon.b2b.controllers.pages.checkout.steps

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import org.springframework.http.HttpStatus
import spock.lang.Specification

import isv.sap.payment.addon.b2b.ReplenishmentInfoData
import isv.sap.payment.addon.b2b.facade.ReplenishmentCheckoutFacade

@UnitTest
class ReplenishmentCheckoutControllerSpec extends Specification
{
    def replenishmentCheckoutFacade = Mock([useObjenesis: false], ReplenishmentCheckoutFacade)

    def controller = new ReplenishmentCheckoutController(replenishmentCheckoutFacade: replenishmentCheckoutFacade)

    @Test
    def 'should add replenishment'()
    {
        given:
        def replenishment = new ReplenishmentInfoData()

        when:
        def response = controller.addReplenishment(replenishment)

        then:
        1 * replenishmentCheckoutFacade.add(replenishment)
        response.statusCode == HttpStatus.OK
    }

    @Test
    def 'should remove replenishment'()
    {
        when:
        def response = controller.removeReplenishment()

        then:
        1 * replenishmentCheckoutFacade.removeReplenishment()
        response.statusCode == HttpStatus.OK
    }
}
