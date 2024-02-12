package isv.sap.payment.service.alternativepayment.handler

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService

import static isv.sap.payment.enums.IsvAlternativePaymentStatus.ABANDONED

@UnitTest
class AlternativePaymentAbandonedOrderHandlerSpec extends Specification
{
    def alternativePaymentOrderStatusService = Mock([useObjenesis: false], AlternativePaymentOrderStatusService)

    def order = Mock([useObjenesis: false], OrderModel)

    def alternativePaymentAbandonedOrderHandler = new AlternativePaymentAbandonedOrderHandler()

    void setup()
    {
        alternativePaymentAbandonedOrderHandler.alternativePaymentOrderStatusService = alternativePaymentOrderStatusService
    }

    @Test
    def 'handle abandoned order'()
    {
        when:
        alternativePaymentAbandonedOrderHandler.handle(order)

        then:
        alternativePaymentOrderStatusService.updateOrderByPaymentStatus(order, ABANDONED)
    }
}
