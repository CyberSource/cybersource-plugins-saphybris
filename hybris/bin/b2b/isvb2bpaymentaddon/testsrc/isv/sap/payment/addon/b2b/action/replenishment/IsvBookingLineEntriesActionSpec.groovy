package isv.sap.payment.addon.b2b.action.replenishment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.ACCOUNT
import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.CARD
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.NOK
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK

@UnitTest
class IsvBookingLineEntriesActionSpec extends Specification
{
    def process = Mock([useObjenesis: false], B2BApprovalProcessModel)

    def order = Mock([useObjenesis: false], OrderModel)

    def modelService = Mock([useObjenesis: false], ModelService)

    def tested = Spy(IsvSetBookingLineEntriesAction)

    void setup()
    {
        process.order >> order
        tested.modelService = modelService
    }

    @Test
    'return OK and skip set booking line entries when Payment Type is Credit Card'()
    {
        given:
        order.paymentType >> CARD

        when:
        def result = tested.executeAction(process)

        then:
        result == OK
        0 * tested.callSupper(_) >> NOK
        1 * modelService.refresh(order)
    }

    @Test
    'return OK and set booking line entries when Payment Type is ACCOUNT'()
    {
        given:
        order.paymentType >> ACCOUNT

        when:
        def result = tested.executeAction(process)

        then:
        result == OK
        1 * tested.callSupper(process) >> OK
        1 * modelService.refresh(order)
    }
}
