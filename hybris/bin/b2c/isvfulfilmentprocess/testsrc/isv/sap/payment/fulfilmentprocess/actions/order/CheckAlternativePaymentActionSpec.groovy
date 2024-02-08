package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import jersey.repackaged.com.google.common.collect.ImmutableMap
import org.junit.Test
import spock.lang.Specification

import static de.hybris.platform.core.enums.OrderStatus.CANCELLED
import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_AUTHORIZED
import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_CAPTURED
import static de.hybris.platform.core.enums.OrderStatus.PAYMENT_NOT_CAPTURED
import static de.hybris.platform.core.enums.OrderStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativePaymentAction.Transition.NOK
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativePaymentAction.Transition.OK
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativePaymentAction.Transition.PAY
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckAlternativePaymentAction.Transition.WAIT

@UnitTest
class CheckAlternativePaymentActionSpec extends Specification
{

    def order = Mock([useObjenesis: false], OrderModel)

    def businessProcess = Mock([useObjenesis: false], OrderProcessModel)

    def checkAlternativePaymentAction = new CheckAlternativePaymentAction()

    void setup()
    {
        businessProcess.order >> order
        checkAlternativePaymentAction.setTransitionMap(ImmutableMap.of(
                PAYMENT_CAPTURED, OK, WAITING_FOR_PAYMENT, WAIT, PAYMENT_NOT_CAPTURED, NOK, PAYMENT_AUTHORIZED, PAY))
    }

    @Test
    def 'action should validate transition value'()
    {
        given:
        order.status >> orderStatus

        when:
        def result = checkAlternativePaymentAction.execute(businessProcess)

        then:
        result == expectedTransition

        where:
        orderStatus          | expectedTransition
        PAYMENT_CAPTURED     | 'OK'
        WAITING_FOR_PAYMENT  | 'WAIT'
        PAYMENT_NOT_CAPTURED | 'NOK'
        PAYMENT_AUTHORIZED   | 'PAY'
    }

    @Test
    def 'should throw an exception if the order status is not mapped to a transtition'()
    {
        given:
        order.status >> CANCELLED

        when:
        checkAlternativePaymentAction.execute(businessProcess)

        then:
        thrown IllegalStateException
    }
}
