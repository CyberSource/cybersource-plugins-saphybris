package isv.sap.payment.fulfilmentprocess.actions.returns

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ReturnAction
import de.hybris.platform.returns.model.ReturnEntryModel
import de.hybris.platform.returns.model.ReturnProcessModel
import de.hybris.platform.returns.model.ReturnRequestModel
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class InitialReturnActionSpec extends Specification
{
    def action = new InitialReturnAction()

    @Test
    @Unroll
    def 'Should determine the return transition based on the actions from the return entry'()
    {
        given:
        def returnRequest = Mock(ReturnRequestModel)
        def returnEntry = Mock(ReturnEntryModel)
        returnEntry.action >> returnRequestParam
        returnRequest.returnEntries >> [returnEntry]

        and:
        def returnProcess = Mock(ReturnProcessModel)
        returnProcess.returnRequest >> returnRequest

        when:
        def transition = action.execute(returnProcess)

        then:
        transition == transitionParam

        where:
        returnRequestParam     | transitionParam
        ReturnAction.IMMEDIATE | InitialReturnAction.Transition.INSTORE.toString()
        ReturnAction.HOLD      | InitialReturnAction.Transition.ONLINE.toString()
    }
}
