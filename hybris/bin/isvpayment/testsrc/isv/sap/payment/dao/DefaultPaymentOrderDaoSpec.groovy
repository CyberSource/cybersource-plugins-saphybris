package isv.sap.payment.dao

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.search.impl.SearchResultImpl
import org.junit.Test
import spock.lang.Specification

@UnitTest
class DefaultPaymentOrderDaoSpec extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def flexibleSearchService = Mock([useObjenesis: false], FlexibleSearchService)

    def dao = new DefaultPaymentOrderDao()

    void setup()
    {
        flexibleSearchService.search('SELECT {pk} FROM {Order} WHERE {guid} = ?guid',
                                     ['guid': '1234567890']) >> new SearchResultImpl(Collections.singletonList(order), 1, 1, 0)
        flexibleSearchService.search('SELECT {pk} FROM {Order} WHERE {status} = ?status',
                                     ['status': OrderStatus.WAITING_FOR_PAYMENT]) >> new SearchResultImpl(Collections.singletonList(order), 1, 1, 0)

        dao.flexibleSearchService = flexibleSearchService
    }

    @Test
    def 'order dao should return order by guid'()
    {
        when:
        def result = dao.findOrderByGuid('1234567890')

        then:
        result == order
    }

    @Test
    def 'order dao should return order by status'()
    {
        when:
        def result = dao.findOrdersByStatus(OrderStatus.WAITING_FOR_PAYMENT)

        then:
        result == [order]
    }
}
