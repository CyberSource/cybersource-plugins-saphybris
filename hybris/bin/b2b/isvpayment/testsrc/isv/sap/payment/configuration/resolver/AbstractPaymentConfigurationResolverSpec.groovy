package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.search.SearchResult
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AbstractPaymentConfigurationResolverSpec extends Specification
{
    def searchQuery = 'flexible search query string'

    def searchResult = Mock(SearchResult)

    @SuppressWarnings('BracesForClass')
    def resolver = new AbstractPaymentConfigurationResolver<Object>() {
        @Override
        String getSearchQuery(final Map<String, Object> params)
        { searchQuery }
    }

    def setup()
    {
        resolver.flexibleSearchService = Mock(FlexibleSearchService)
        resolver.flexibleSearchService.search(_) >> searchResult
    }

    @Test
    def 'should resolve configuration'()
    {
        given:
        def expected = new Object()
        searchResult.result >> [expected]

        when:
        def actual = resolver.resolve([:])

        then:
        actual == expected
    }

    @Test
    def 'should throw exception when no configuration resolved'()
    {
        given:
        searchResult.result >> []

        when:
        resolver.resolve([:])

        then:
        thrown(ModelNotFoundException)
    }

    @Test
    def 'should throw exception when more than one configuration resolved'()
    {
        given:
        searchResult.result >> [new Object(), new Object()]

        when:
        resolver.resolve([:])

        then:
        thrown(AmbiguousIdentifierException)
    }
}
