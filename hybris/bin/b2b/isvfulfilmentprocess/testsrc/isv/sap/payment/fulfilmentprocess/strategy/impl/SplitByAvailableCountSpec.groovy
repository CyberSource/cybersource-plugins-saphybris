package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.stock.CommerceStockService
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.store.BaseStoreModel
import de.hybris.platform.storelocator.model.PointOfServiceModel
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class SplitByAvailableCountSpec extends Specification
{
    def commerceStockService = Mock(CommerceStockService)

    def strategy = new SplitByAvailableCount(commerceStockService: commerceStockService)

    @Test
    @Unroll
    def 'Should return boolean value indicating if stock is available for product and point of service'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def order = Mock(OrderModel)
        orderEntry.order >> order
        orderEntry.quantity >> 8
        orderEntry.product >> Stub(ProductModel)
        def pointOfService = Mock(PointOfServiceModel)
        orderEntry.deliveryPointOfService >> pointOfService

        and:
        commerceStockService.getStockLevelForProductAndPointOfService(_ as ProductModel, _ as PointOfServiceModel) >> stockLevelParam

        when:
        def result = strategy.getGroupingObject(orderEntry)

        then:
        result == expectedResult

        where:
        stockLevelParam | expectedResult
        10              | true
        null            | true
        5               | false
    }

    @Test
    @Unroll
    def 'Should return boolean value indicating if stock is available for product and base store'()
    {
        given:
        def orderEntry = Mock(AbstractOrderEntryModel)
        def order = Mock(OrderModel)
        orderEntry.order >> order
        orderEntry.quantity >> 8
        orderEntry.product >> Stub(ProductModel)
        def baseStore = Mock(BaseStoreModel)
        order.store >> baseStore

        and:
        commerceStockService.getStockLevelForProductAndBaseStore(_ as ProductModel, _ as BaseStoreModel) >> stockLevelParam

        when:
        def result = strategy.getGroupingObject(orderEntry)

        then:
        result == expectedResult

        where:
        stockLevelParam | expectedResult
        10              | true
        null            | true
        5               | false
    }
}
