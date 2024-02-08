package isv.sap.payment.report.listener.conversion.decision;

import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marks order a fraud and block the order process
 */
public class RejectDecisionStrategy extends AbstractDecisionStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger(RejectDecisionStrategy.class);

    @Override
    public void updateOrderFraudStatus(final OrderModel order)
    {
        final FraudReportModel fraudReport = createFraudReport(order, FraudStatus.FRAUD);
        final OrderHistoryEntryModel historyEntry = createHistoryLog(order, FraudStatus.FRAUD);

        order.setStatus(OrderStatus.FRAUD);
        order.setFraudulent(Boolean.TRUE);

        LOG.debug("Set order [{}] properties: status = [{}], fraudulent = [{}]", order.getCode(), OrderStatus.FRAUD,
                Boolean.TRUE);

        getModelService().saveAll(fraudReport, historyEntry, order);
    }

    @Override
    public boolean supports(final String decision)
    {
        return "REVIEWREJECT".equalsIgnoreCase(decision);
    }
}
