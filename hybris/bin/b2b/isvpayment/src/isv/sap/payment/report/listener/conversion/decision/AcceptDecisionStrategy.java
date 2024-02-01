package isv.sap.payment.report.listener.conversion.decision;

import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for unblock order process
 */
public class AcceptDecisionStrategy extends AbstractDecisionStrategy
{
    private static final Logger LOG = LoggerFactory.getLogger(AcceptDecisionStrategy.class);

    @Override
    public void updateOrderFraudStatus(final OrderModel order)
    {
        final FraudReportModel fraudReport = createFraudReport(order, FraudStatus.OK);
        final OrderHistoryEntryModel historyEntry = createHistoryLog(order, FraudStatus.OK);

        order.setStatus(OrderStatus.FRAUD_CHECKED);
        order.setFraudulent(Boolean.FALSE);

        LOG.debug("Set order [{}] properties: status = [{}], fraudulent = [{}]", order.getCode(),
                OrderStatus.FRAUD_CHECKED, Boolean.FALSE);

        getModelService().saveAll(fraudReport, historyEntry, order);
    }

    @Override
    public boolean supports(final String decision)
    {
        return "REVIEWACCEPT".equalsIgnoreCase(decision);
    }
}
