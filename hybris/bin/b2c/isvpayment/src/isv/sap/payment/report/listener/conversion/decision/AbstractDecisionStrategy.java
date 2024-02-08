package isv.sap.payment.report.listener.conversion.decision;

import java.util.Date;
import javax.annotation.Resource;

import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * Defines common methods used by descendents strategies
 */
public abstract class AbstractDecisionStrategy implements DecisionChangeStrategy
{
    @Resource
    private ModelService modelService;

    protected FraudReportModel createFraudReport(final OrderModel order, final FraudStatus status)
    {
        final FraudReportModel fraudReport = getModelService().create(FraudReportModel.class);
        fraudReport.setOrder(order);
        fraudReport.setStatus(status);
        fraudReport.setProvider("isv");
        fraudReport.setTimestamp(new Date());
        fraudReport.setCode(order.getCode() + "_" + order.getFraudReports().size());

        return fraudReport;
    }

    protected OrderHistoryEntryModel createHistoryLog(final OrderModel order, final FraudStatus fraudStatus)
    {
        final OrderHistoryEntryModel historyEntry = getModelService().create(OrderHistoryEntryModel.class);
        historyEntry.setTimestamp(new Date());
        historyEntry.setOrder(order);
        historyEntry.setDescription("Fraud status set to " + fraudStatus.getCode());

        return historyEntry;
    }

    protected ModelService getModelService()
    {
        return modelService;
    }
}
