package isv.sap.payment.service.alternativepayment

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.enums.CheckStatusDecision
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.service.alternativepayment.handler.AlternativePaymentAbandonedOrderHandler
import isv.sap.payment.service.alternativepayment.handler.DefaultPaymentPendingOrderHandler
import isv.sap.payment.service.alternativepayment.handler.PayPalPaymentPendingOrderHandler

@UnitTest
class AlternativePaymentOrderStatusContextSpec extends Specification
{
    def abandonedOrderHandler = Mock(AlternativePaymentAbandonedOrderHandler)

    def defaultPendingOrderHandler = Mock(DefaultPaymentPendingOrderHandler)

    def paypalPendingOrderHandler = Mock(PayPalPaymentPendingOrderHandler)

    def pendingOrderHandlersMap = [
            (AlternativePaymentMethod.PPL): paypalPendingOrderHandler
    ]

    def context = new AlternativePaymentOrderStatusContext(
            pendingOrderHandlersMap: pendingOrderHandlersMap,
            abandonedOrderHandler: abandonedOrderHandler,
            defaultPendingOrderHandler: defaultPendingOrderHandler
    )

    @Test
    @Unroll
    def 'should resolve correct order handler'()
    {
        when:
        def handler = context.getOrderHandler(checkStatusDecision, paymentMethod)

        then:
        resultHandler.isInstance(handler)

        where:
        checkStatusDecision       | paymentMethod                    || resultHandler
        CheckStatusDecision.ERROR | AlternativePaymentMethod.KLI || AlternativePaymentAbandonedOrderHandler
        CheckStatusDecision.RUN   | AlternativePaymentMethod.KLI || DefaultPaymentPendingOrderHandler
        CheckStatusDecision.RUN   | AlternativePaymentMethod.PPL || PayPalPaymentPendingOrderHandler
    }
}
