package isv.sap.payment.configuration.resolver

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import static isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel.PAYMENTMETHOD
import static isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel._TYPECODE

@UnitTest
class DefaultAlternativePaymentConfigurationResolverSpec extends Specification
{
    @Test
    def 'should provide search query'()
    {
        given:
        def params = [:]

        when:
        def searchQuery = new DefaultAlternativePaymentConfigurationResolver().getSearchQuery(params)

        then:
        searchQuery == "SELECT {pk} FROM {${_TYPECODE}} WHERE {${PAYMENTMETHOD}}=?paymentMethod"
    }
}
