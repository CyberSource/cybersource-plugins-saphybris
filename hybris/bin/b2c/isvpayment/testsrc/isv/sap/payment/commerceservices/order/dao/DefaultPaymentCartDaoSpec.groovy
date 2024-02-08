package isv.sap.payment.commerceservices.order.dao

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.search.SearchResult
import org.junit.Test
import spock.lang.Specification

@UnitTest
class DefaultPaymentCartDaoSpec extends Specification
{
    def cart = Mock([useObjenesis: false], CartModel)

    def searchResult = Mock([useObjenesis: false], SearchResult)

    def flexibleSearchService = Mock([useObjenesis: false], FlexibleSearchService)

    def dao = Spy(DefaultPaymentCartDao)

    def setup()
    {
        dao.flexibleSearchService = flexibleSearchService

        searchResult.result >> [cart]
    }

    @Test
    def 'should return cart by given guid'()
    {
        given:
        dao.doSearch(_ as String, [guid: '12345'], CartModel) >> [cart]

        when:
        def resultCart = dao.getCartForGuid('12345')

        then:
        resultCart == cart
    }

    @Test
    def 'should return null if cart not found'()
    {
        given:
        dao.doSearch(_ as String, [guid: '12345'], CartModel) >> []

        when:
        def resultCart = dao.getCartForGuid('12345')

        then:
        resultCart == null
    }
}
