package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class ReserveOrderAmountActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new ReserveOrderAmountAction(
            modelService: modelService
    )

    @Test
    def 'Should mark order as reserved'()
    {
        given:
        def order = Mock(OrderModel)
        def orderProcess = Mock(OrderProcessModel)
        orderProcess.order >> order

        when:
        action.executeAction(orderProcess)

        then:
        1 * order.setStatus(OrderStatus.PAYMENT_AMOUNT_RESERVED)
        1 * modelService.save(order)
    }
}
