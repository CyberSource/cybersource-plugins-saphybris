package isv.sap.payment.addon.b2b.fulfilmentprocess.actions

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class B2BCheckOrderPaymentTypeActionSpec extends Specification
{
    def action = new B2BCheckOrderPaymentTypeAction()

    def process = new OrderProcessModel()

    def order = new OrderModel(paymentTransactions: [])

    @Test
    def 'execute: Should return NOK if order is not present'()
    {
        expect:
        action.execute(process) == 'NOK'
    }

    @Test
    def 'execute: Should return ACCOUNT if order was paid from account'()
    {
        given:
        process.order = order
        order.paymentType = CheckoutPaymentType.ACCOUNT

        expect:
        action.execute(process) == 'ACCOUNT'
    }

    @Test
    def 'execute: Should select b2c path if payment type is not ACCOUNT'()
    {
        given:
        process.order = order

        expect:
        action.execute(process) == 'NOK'
    }
}
