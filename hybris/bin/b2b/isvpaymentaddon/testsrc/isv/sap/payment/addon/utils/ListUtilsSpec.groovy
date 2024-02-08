package isv.sap.payment.addon.utils

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class ListUtilsSpec extends Specification
{
    @Test
    def 'list values should be concatenated skipping excluded'()
    {
        when:
        def result = ListUtils.toString(['value1', 'value2', 'value3', 'value4'], ['value2', 'value4'])

        then:
        result == 'value1,value3'
    }

    @Test
    def 'list values should be concatenated'()
    {
        when:
        def result = ListUtils.toString(['value1', 'value2'])

        then:
        result == 'value1,value2'
    }
}
