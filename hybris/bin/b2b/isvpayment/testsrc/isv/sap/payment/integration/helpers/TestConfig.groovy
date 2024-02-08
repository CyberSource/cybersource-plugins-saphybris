package isv.sap.payment.integration.helpers

import org.apache.commons.configuration.CompositeConfiguration
import org.apache.commons.configuration.ConfigurationException
import org.apache.commons.configuration.PropertiesConfiguration

import static java.lang.String.format

class TestConfig
{
    public static final String MERCHANT_REGION_DEFAULT = 'default'
    public static final String MERCHANT_REGION_EUROPE = 'europe'
    public static final String MERCHANT_REGION_US = 'us'

    String merchant
    String userName
    String token
    Double processorErrorTrigger
    String nameReviewTrigger
    String payPalPayerId
    String payPalPayerEmail
    String returnUrl
    String cancelUrl
    String failUrl
    String idealOption
    String klarnaCancelUrl
    String klarnaFailureUrl
    String klarnaSuccessUrl

    def getTestConfig(configDir, String merchantRegion = MERCHANT_REGION_DEFAULT)
    {
        def compositeConfiguration = new CompositeConfiguration()

        try
        {
            def localConfig = new PropertiesConfiguration("${configDir}/test.properties")
            compositeConfiguration.addConfiguration(localConfig)

            def config = new PropertiesConfiguration('test.properties')
            compositeConfiguration.addConfiguration(config)
        }
        catch (ConfigurationException ignored)
        {
        }

        setConstants(compositeConfiguration, merchantRegion)
        this
    }

    private setConstants(compositeConfiguration, String merchantRegion)
    {
        merchant = compositeConfiguration.getProperty(format('isv.payment.test.merchant.%s.id', merchantRegion)) as String
        userName = compositeConfiguration.getProperty(format('isv.payment.test.merchant.%s.username', merchantRegion)) as String
        token = compositeConfiguration.getProperty(format('isv.payment.test.merchant.%s.token', merchantRegion)) as String
        processorErrorTrigger = compositeConfiguration.getProperty('isv.payment.test.processor.error.code') as Double
        nameReviewTrigger = compositeConfiguration.getProperty('isv.payment.test.review.name') as String
        payPalPayerId = compositeConfiguration.getProperty('isv.payment.test.paypal.payer.id') as String
        payPalPayerEmail = compositeConfiguration.getProperty('isv.payment.test.paypal.payer.email') as String
        returnUrl = compositeConfiguration.getProperty('isv.payment.test.return.url') as String
        cancelUrl = compositeConfiguration.getProperty('isv.payment.test.cancel.url') as String
        failUrl = compositeConfiguration.getProperty('isv.payment.test.fail.url') as String
        idealOption = compositeConfiguration.getProperty('isv.payment.test.ideal.option') as String
        klarnaCancelUrl = compositeConfiguration.getProperty('isv.payment.test.klarna.cancel.url') as String
        klarnaFailureUrl = compositeConfiguration.getProperty('isv.payment.test.klarna.failure.url') as String
        klarnaSuccessUrl = compositeConfiguration.getProperty('isv.payment.test.klarna.success.url') as String
    }
}
