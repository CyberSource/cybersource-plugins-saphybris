package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel._TYPECODE

@UnitTest
class MerchantPaymentConfigurationResolverSpec extends Specification
{
    @Test
    def 'should provide search query'()
    {
        given:
        def params = [site: 'site', currency: 'currency']

        when:
        def searchQuery = new MerchantPaymentConfigurationResolver().getSearchQuery(params)

        then:
        searchQuery == "SELECT {pk} FROM {${_TYPECODE}} WHERE {site}=?site AND {currency}=?currency"
    }
}
