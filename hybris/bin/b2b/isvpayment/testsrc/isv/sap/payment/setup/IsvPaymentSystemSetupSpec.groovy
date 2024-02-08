package isv.sap.payment.setup

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.setup.SetupImpexService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class IsvPaymentSystemSetupSpec extends Specification
{
    @Test
    def 'setup should import essential data'()
    {
        given:
        def setupImpexService = Mock([useObjenesis: false], SetupImpexService)

        when:
        new IsvPaymentSystemSetup(setupImpexService: setupImpexService).createEssentialData()

        then:
        1 * setupImpexService.importImpexFile('/import/cronjobs.impex', true)
    }
}
