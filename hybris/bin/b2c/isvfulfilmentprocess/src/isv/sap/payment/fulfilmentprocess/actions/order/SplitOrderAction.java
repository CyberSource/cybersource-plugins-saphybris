package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.ArrayList;
import java.util.List;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.OrderSplittingService;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class SplitOrderAction extends AbstractProceduralAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SplitOrderAction.class);

    private OrderSplittingService orderSplittingService;

    private BusinessProcessService businessProcessService;

    @Override
    public void executeAction(final OrderProcessModel process) throws Exception // NOPMD
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
        final List<AbstractOrderEntryModel> entriesToSplit = new ArrayList<>();
        for (final AbstractOrderEntryModel entry : process.getOrder().getEntries())
        {
            if (entry.getConsignmentEntries() == null || entry.getConsignmentEntries().isEmpty())
            {
                entriesToSplit.add(entry);
            }
        }

        final List<ConsignmentModel> consignments = getOrderSplittingService()
                .splitOrderForConsignment(process.getOrder(),
                        entriesToSplit);

        LOG.debug("Splitting order into {} consignments.", consignments.size());

        int index = 0;
        for (final ConsignmentModel consignment : consignments)
        {
            final ConsignmentProcessModel subProcess = getBusinessProcessService().<ConsignmentProcessModel>createProcess(

                    process.getCode() + "_" + (++index),
                    IsvfulfilmentprocessConstants.CONSIGNMENT_SUBPROCESS_NAME);

            subProcess.setParentProcess(process);
            subProcess.setConsignment(consignment);
            save(subProcess);

            getBusinessProcessService().startProcess(subProcess);
        }
        setOrderStatus(process.getOrder(), OrderStatus.ORDER_SPLIT);
    }

    protected OrderSplittingService getOrderSplittingService()
    {
        return orderSplittingService;
    }

    @Required
    public void setOrderSplittingService(final OrderSplittingService orderSplittingService)
    {
        this.orderSplittingService = orderSplittingService;
    }

    protected BusinessProcessService getBusinessProcessService()
    {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService)
    {
        this.businessProcessService = businessProcessService;
    }
}
