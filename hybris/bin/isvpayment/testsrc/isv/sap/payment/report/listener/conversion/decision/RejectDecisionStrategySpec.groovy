package isv.sap.payment.report.listener.conversion.decision

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.FraudStatus
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.fraud.model.FraudReportModel
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class RejectDecisionStrategySpec extends Specification
{
    def report = Mock([useObjenesis: false], FraudReportModel)

    def order = Mock([useObjenesis: false], OrderModel)

    def historyEntry = Mock([useObjenesis: false], OrderHistoryEntryModel)

    def modelService = Mock([useObjenesis: false], ModelService)

    def strategy = Spy(RejectDecisionStrategy)

    @Test
    def 'strategy should marck order as fraudulent'()
    {
        when:
        strategy.updateOrderFraudStatus(order)

        then:
        1 * strategy.modelsService >> modelService
        1 * strategy.createFraudReport(order, FraudStatus.FRAUD) >> report
        1 * strategy.createHistoryLog(order, FraudStatus.FRAUD) >> historyEntry
        1 * order.setStatus(OrderStatus.FRAUD)
        1 * order.setFraudulent(Boolean.TRUE)
        1 * modelService.saveAll(report, historyEntry, order)
    }
}
