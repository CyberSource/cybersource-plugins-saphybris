package isv.sap.payment.sampledata.setup

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.setup.SetupImpexService
import org.junit.Test
import spock.lang.Specification

import static IsvpaymentsampledataSystemSetup.DATA_FOLDER

@UnitTest
class IsvpaymentsampledataSystemSetupSpec extends Specification
{
    def systemSetup = new IsvpaymentsampledataSystemSetup()

    def setupImpexService = Mock(useObjesis: false, SetupImpexService)

    def 'setup'()
    {
        systemSetup.setupImpexService = setupImpexService
    }

    @Test
    def 'should create essential data on setup'()
    {
        when:
        systemSetup.createEssentialData()

        then:
        (1.._) * setupImpexService.importImpexFile(DATA_FOLDER + 'import/common/merchant_payment_configuration.impex', true)
        _ * setupImpexService.importImpexFile(_)
    }
}
