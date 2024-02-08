package isv.sap.payment.fulfilmentprocess.actions.order;

import java.util.Collection;
import java.util.Optional;

import de.hybris.platform.core.enums.DeliveryStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubprocessesCompletedAction extends AbstractSimpleDecisionAction<OrderProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(SubprocessesCompletedAction.class);

    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());
        LOG.info("Process: {} is checking for {}  subprocess results", process.getCode(),
                process.getConsignmentProcesses().size());

        final OrderModel order = process.getOrder();
        final Collection<ConsignmentProcessModel> consignmentProcesses = process.getConsignmentProcesses();

        if (CollectionUtils.isNotEmpty(consignmentProcesses))
        {
            final Optional<ConsignmentProcessModel> atleastOneConsProcessNotDone = consignmentProcesses.stream()
                    .filter(consProcess -> !consProcess.isDone())
                    .findFirst();
            final boolean allConsProcessNotDone = consignmentProcesses.stream()
                    .noneMatch(ConsignmentProcessModel::isDone);

            if (allConsProcessNotDone)
            {
                LOG.info("Process: {} found all subprocesses incomplete", process.getCode());
                order.setDeliveryStatus(DeliveryStatus.NOTSHIPPED);
                save(order);
                return Transition.NOK;
            }
            else if (atleastOneConsProcessNotDone.isPresent())
            {
                LOG.info("Process: {} found subprocess {} incomplete -> wait again!", process.getCode(),
                        atleastOneConsProcessNotDone.get()
                                .getCode());
                order.setDeliveryStatus(DeliveryStatus.PARTSHIPPED);
                save(order);
                return Transition.NOK;
            }
        }

        LOG.info("Process: {} found all subprocesses complete", process.getCode());
        order.setDeliveryStatus(DeliveryStatus.SHIPPED);
        save(order);
        return Transition.OK;
    }
}
