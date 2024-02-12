package isv.sap.payment.fulfilmentprocess.actions.returns

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ReturnStatus
import de.hybris.platform.returns.model.ReturnEntryModel
import de.hybris.platform.returns.model.ReturnProcessModel
import de.hybris.platform.returns.model.ReturnRequestModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class InventoryUpdateActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new InventoryUpdateAction(modelService: modelService)

    @Test
    def 'Should update return request as completed'()
    {
        given:
        def returnProcess = Mock(ReturnProcessModel)
        def returnRequest = Mock(ReturnRequestModel)
        def returnEntry = Mock(ReturnEntryModel)
        returnRequest.returnEntries >> [returnEntry]
        returnProcess.returnRequest >> returnRequest

        when:
        action.execute(returnProcess)

        then:
        1 * returnEntry.setStatus(ReturnStatus.COMPLETED)
        1 * modelService.save(returnEntry)

        and:
        1 * returnRequest.setStatus(ReturnStatus.COMPLETED)
        1 * modelService.save(returnRequest)
    }
}
