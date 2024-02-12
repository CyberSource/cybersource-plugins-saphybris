package isv.sap.payment.report.listener.conversion;

import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.cjl.payment.model.ConversionReportInfo;
import isv.sap.payment.dao.PaymentOrderDao;
import isv.sap.payment.report.listener.ReportListener;
import isv.sap.payment.report.listener.conversion.decision.DecisionChangeStrategy;

import static java.util.Objects.nonNull;

/**
 * Conversion report listener responsible for block/unblock order process based on a conversion decision
 */
public class ProcessConversionReportListener implements ReportListener<ConversionReportInfo>
{
    private static final Logger LOG = LoggerFactory.getLogger(ProcessConversionReportListener.class);

    @Resource(name = "isv.sap.payment.orderDao")
    private PaymentOrderDao orderDao;

    @Resource
    private BusinessProcessService businessProcessService;

    private List<DecisionChangeStrategy> decisionStrategies;

    private static boolean shouldProcessOrder(final AbstractOrderModel order)
    {
        return nonNull(order) && OrderStatus.FRAUD_DECISION.equals(order.getStatus());
    }

    @Override
    public void handle(final Collection<ConversionReportInfo> conversions)
    {
        conversions.forEach(conversion ->
        {
            try
            {
                final OrderModel order = orderDao.findOrderByGuid(conversion.getMerchantReferenceCode());
                if (shouldProcessOrder(order))
                {
                    final DecisionChangeStrategy decisionStrategy = getDecisionStrategy(
                            StringUtils.upperCase(conversion.getOriginalDecision() + conversion.getNewDecision()));

                    decisionStrategy.updateOrderFraudStatus(order);
                    triggerOrderInReviewUnblockedEvent(order);

                    LOG.info("Order: {} checked for fraud. Order status: {}", order.getCode(), order.getStatus());
                }
            }
            catch (final Exception e)
            {
                LOG.warn("Error while updating Order: {} for fraud", conversion.getMerchantReferenceCode(), e);
            }
        });
    }

    private DecisionChangeStrategy getDecisionStrategy(final String originalDecision)
    {
        return decisionStrategies.stream()
                .filter(strategy -> strategy.supports(originalDecision))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No decision strategy for: " + originalDecision));
    }

    private void triggerOrderInReviewUnblockedEvent(final OrderModel order)
    {
        businessProcessService.triggerEvent(order.getCode() + "_ReviewDecision");
    }

    @Required
    public void setDecisionStrategies(final List<DecisionChangeStrategy> decisionStrategies)
    {
        this.decisionStrategies = decisionStrategies;
    }
}
