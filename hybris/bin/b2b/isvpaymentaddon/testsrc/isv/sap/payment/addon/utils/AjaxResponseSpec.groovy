package isv.sap.payment.addon.utils

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AjaxResponseSpec extends Specification
{
    @Test
    def 'Should keep correct status'()
    {
        expect:
        AjaxResponse.success().success
        !AjaxResponse.fail().success
    }

    @Test
    def 'Should store and return consistent data'()
    {
        expect:
        AjaxResponse.success().put('a', 1).put('b', 2).put('c', 3).data == ['a': 1, 'b': 2, 'c': 3]
    }
}
