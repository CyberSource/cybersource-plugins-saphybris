package isv.sap.payment.fulfilmentprocess.actions.consignment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class WaitBeforeTransmissionActionSpec extends Specification
{
    def action = new WaitBeforeTransmissionAction()

    @Test
    def 'Should return ok transition'()
    {
        when:
        def transition = action.execute(Stub(ConsignmentProcessModel))

        then:
        transition == 'OK'
    }
}
