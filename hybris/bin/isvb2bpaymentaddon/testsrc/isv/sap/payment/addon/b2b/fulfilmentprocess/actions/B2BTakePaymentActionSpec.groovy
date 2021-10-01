package isv.sap.payment.addon.b2b.fulfilmentprocess.actions

import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

class B2BTakePaymentActionSpec extends Specification
{
    def action = new B2BTakePaymentAction()

    def modelService = Mock([useObjenesis: false], ModelService)

    def process = new OrderProcessModel()

    def order = new OrderModel()

    def setup()
    {
        process.order = order
        action.modelService = modelService
    }

    @Test
    def 'executeAction: should pass order if it was paid by ACCOUNT'()
    {
        given:
        order.paymentType = CheckoutPaymentType.ACCOUNT

        when:
        def result = action.execute(process)

        then:
        result == AbstractSimpleDecisionAction.Transition.OK.toString()
        order.status == OrderStatus.PAYMENT_CAPTURED
    }
}
