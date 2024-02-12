package isv.sap.payment.fulfilmentprocess.actions.returns

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.ReturnStatus
import de.hybris.platform.returns.model.ReturnProcessModel
import de.hybris.platform.returns.model.ReturnRequestModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class TaxReverseActionSpec extends Specification
{
    def modelService = Mock(ModelService)

    def action = new TaxReverseAction(modelService: modelService)

    @Test
    def 'Should update return request as tax reversed'()
    {
        given:
        def returnProcess = Mock(ReturnProcessModel)
        def returnRequest = Mock(ReturnRequestModel)
        returnProcess.returnRequest >> returnRequest

        when:
        action.execute(returnProcess)

        then:
        1 * returnRequest.setStatus(ReturnStatus.TAX_REVERSED)
        1 * modelService.save(returnRequest)
    }
}
