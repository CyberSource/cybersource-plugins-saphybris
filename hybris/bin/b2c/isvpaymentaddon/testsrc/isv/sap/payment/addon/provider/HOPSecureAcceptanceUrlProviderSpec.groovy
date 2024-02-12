package isv.sap.payment.addon.provider

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.site.BaseSiteService
import org.apache.commons.configuration.BaseConfiguration
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.addon.provider.HOPSecureAcceptanceUrlProvider.AUTHORIZATION_URL_KEY
import static isv.sap.payment.addon.provider.HOPSecureAcceptanceUrlProvider.SUBSCRIPTION_URL_KEY

@UnitTest
class HOPSecureAcceptanceUrlProviderSpec extends Specification
{
    def provider = new HOPSecureAcceptanceUrlProvider()

    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    def baseSiteService = Mock([useObjenesis: false], BaseSiteService)

    def 'setup'()
    {
        def configuration = new BaseConfiguration()

        configuration.addProperty(SUBSCRIPTION_URL_KEY, 'subscription_url')
        configuration.addProperty(AUTHORIZATION_URL_KEY, 'authorization_url')
        configuration.addProperty('secure.acceptance.auth.transaction.type', 'authorization')
        configuration.addProperty('secure.acceptance.subscription.transaction.type', 'create_payment_token')

        configurationService.configuration >> configuration

        provider.configurationService = configurationService
        provider.baseSiteService = baseSiteService
    }

    @Test
    def 'should provide subscription based url'()
    {
        given:
        baseSiteService.currentBaseSite >> new BaseSiteModel(uid: 'auth')

        when:
        def hopUrl = provider.getURL()

        then:
        hopUrl == 'authorization_url'
    }

    @Test
    def 'should provide authorization based url'()
    {
        given:
        baseSiteService.currentBaseSite >> new BaseSiteModel(uid: 'subscription')

        when:
        def hopUrl = provider.getURL()

        then:
        hopUrl == 'subscription_url'
    }
}
