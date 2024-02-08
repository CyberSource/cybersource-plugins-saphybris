package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class DefaultPaymentConfigurationResolverSpec extends Specification
{
    @Test
    def 'should provide initially configured search query'()
    {
        when:
        def searchQuery = new DefaultPaymentConfigurationResolver().getSearchQuery([:])

        then:
        searchQuery == DefaultPaymentConfigurationResolver.QUERY_TEMPLATE
    }
}
