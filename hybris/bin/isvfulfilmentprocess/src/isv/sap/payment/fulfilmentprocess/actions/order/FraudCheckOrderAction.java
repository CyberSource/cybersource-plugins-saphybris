package isv.sap.payment.fulfilmentprocess.actions.order;

import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class FraudCheckOrderAction extends AbstractFraudCheckAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(FraudCheckOrderAction.class);

    private FraudService fraudService;

    private String providerName;

    protected FraudService getFraudService()
    {
        return fraudService;
    }

    @Required
    public void setFraudService(final FraudService fraudService)
    {
        this.fraudService = fraudService;
    }

    protected String getProviderName()
    {
        return providerName;
    }

    @Required
    public void setProviderName(final String providerName)
    {
        this.providerName = providerName;
    }

    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
        ServicesUtil.validateParameterNotNull(process, "Process can not be null");
        ServicesUtil.validateParameterNotNull(process.getOrder(), "Order can not be null");

        final double scoreLimit = Double.parseDouble(
                Config.getParameter(IsvfulfilmentprocessConstants.EXTENSIONNAME + ".fraud.scoreLimitExternal"));
        final double scoreTolerance = Double.parseDouble(Config.getParameter(
                IsvfulfilmentprocessConstants.EXTENSIONNAME + ".fraud.scoreToleranceExternal"));

        final OrderModel order = process.getOrder();
        final FraudServiceResponse response = getFraudService().recognizeOrderSymptoms(getProviderName(), order);
        final double score = response.getScore();
        if (score < scoreLimit)
        {
            final FraudReportModel fraudReport = createFraudReport(providerName, response, order, FraudStatus.OK);
            final OrderHistoryEntryModel historyEntry = createHistoryLog(providerName, order, FraudStatus.OK, null);
            order.setFraudulent(Boolean.FALSE);
            order.setPotentiallyFraudulent(Boolean.FALSE);
            order.setStatus(OrderStatus.FRAUD_CHECKED);
            modelService.save(fraudReport);
            modelService.save(historyEntry);
            modelService.save(order);
            return Transition.OK;
        }
        else if (score < scoreLimit + scoreTolerance)
        {
            final FraudReportModel fraudReport = createFraudReport(providerName, response, order, FraudStatus.CHECK);
            final OrderHistoryEntryModel historyEntry = createHistoryLog(providerName, order, FraudStatus.CHECK,
                    fraudReport.getCode());
            order.setFraudulent(Boolean.FALSE);
            order.setPotentiallyFraudulent(Boolean.TRUE);
            order.setStatus(OrderStatus.FRAUD_CHECKED);
            modelService.save(fraudReport);
            modelService.save(historyEntry);
            modelService.save(order);
            return Transition.POTENTIAL;
        }
        else
        {
            final FraudReportModel fraudReport = createFraudReport(providerName, response, order, FraudStatus.FRAUD);
            final OrderHistoryEntryModel historyEntry = createHistoryLog(providerName, order, FraudStatus.FRAUD,
                    fraudReport.getCode());
            order.setFraudulent(Boolean.TRUE);
            order.setPotentiallyFraudulent(Boolean.FALSE);
            order.setStatus(OrderStatus.FRAUD_CHECKED);
            modelService.save(fraudReport);
            modelService.save(historyEntry);
            modelService.save(order);
            return Transition.FRAUD;
        }
    }
}
