package isv.sap.payment.report.listener.conversion.decision

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.FraudStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.fraud.model.FraudReportModel
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AbstractDecisionStrategySpec extends Specification
{
    def modelService = Mock(ModelService)

    @SuppressWarnings('BracesForClass')
    def strategy = Spy(new AbstractDecisionStrategy() {
        @Override
        void updateOrderFraudStatus(final OrderModel order)
        {
            throw new UnsupportedOperationException('should not be invoked here')
        }

        @Override
        boolean supports(final String decision)
        {
            false
        }
    })

    def setup()
    {
        strategy.modelService >> modelService
    }

    @Test
    def 'Should create fraud report'()
    {
        given:
        def order = Mock(OrderModel)
        order.code >> 'order12345'
        order.fraudReports >> []

        and:
        def reportModel = Mock(FraudReportModel)
        modelService.create(_) >> reportModel

        when:
        strategy.createFraudReport(order, FraudStatus.FRAUD)

        then:
        1 * reportModel.setOrder(order)
        1 * reportModel.setStatus(FraudStatus.FRAUD)
        1 * reportModel.setProvider('isv')
        1 * reportModel.setTimestamp(_ as Date)
        1 * reportModel.setCode('order12345_0')
    }

    @Test
    def 'Should create history log'()
    {
        given:
        def order = Mock(OrderModel)
        order.code >> 'order12345'

        and:
        def orderHistoryEntry = Mock(OrderHistoryEntryModel)
        modelService.create(_) >> orderHistoryEntry

        when:
        strategy.createHistoryLog(order, FraudStatus.FRAUD)

        then:
        1 * orderHistoryEntry.setOrder(order)
        1 * orderHistoryEntry.setDescription("Fraud status set to ${FraudStatus.FRAUD}")
        1 * orderHistoryEntry.setTimestamp(_ as Date)
    }
}
