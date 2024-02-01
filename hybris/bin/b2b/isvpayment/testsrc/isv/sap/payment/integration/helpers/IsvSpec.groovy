package isv.sap.payment.integration.helpers

import de.hybris.bootstrap.config.ConfigUtil
import spock.lang.Specification

class IsvSpec extends Specification
{
    def configDir = ConfigUtil.getPlatformConfig(this.getClass()).systemConfig.getDir('HYBRIS_CONFIG_DIR')
    def testConfig = new TestConfig().getTestConfig(configDir)
    String merchant = testConfig.merchant
}
