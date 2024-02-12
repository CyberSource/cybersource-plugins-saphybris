package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.model.IsvMerchantModel._TYPECODE

@UnitTest
class MerchantResolverSpec extends Specification
{
    @Test
    def 'should provide search query'()
    {
        given:
        def params = [:]

        when:
        def searchQuery = new MerchantResolver().getSearchQuery(params)

        then:
        searchQuery == "SELECT {pk} FROM {${_TYPECODE}} WHERE {id} = ?id"
    }
}
