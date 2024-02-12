package isv.sap.payment.integration.helpers

import de.hybris.bootstrap.config.ConfigUtil

import isv.cjl.payment.model.Merchant
import isv.sap.payment.service.IsvHybrisMerchantService

class MockMerchantService extends IsvHybrisMerchantService
{
    private final configDir = ConfigUtil.getPlatformConfig(this.getClass()).systemConfig.getDir('HYBRIS_CONFIG_DIR')

    def testConfig = new TestConfig().getTestConfig(configDir)

    @Override
    Merchant getMerchant(final String merchantId)
    {
        new Merchant(testConfig.merchant, credentialHolderFactory.getCredentialHolder(testConfig.token))
    }
}
