package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.MapConfiguration
import org.junit.Test
import spock.lang.Specification

@UnitTest
class IsvConfigurationResolverSpec extends Specification
{
    def map = [:]

    @SuppressWarnings('BracesForClass')
    def resolver = new IsvConfigurationResolver() {
        Configuration resolve()
        {
            MapConfiguration mapConfiguration = new MapConfiguration(map)

            mapConfiguration
        }
    }

    @Test
    def 'should provide property value'()
    {
        when:
        map.put('merchantID', '12345')

        then:
        resolver.resolve().getString('merchantID') == '12345'
    }
}
