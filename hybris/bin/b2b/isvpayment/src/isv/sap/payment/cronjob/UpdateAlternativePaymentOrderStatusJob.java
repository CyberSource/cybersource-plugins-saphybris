package isv.sap.payment.cronjob;

import javax.annotation.Resource;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.enums.CheckStatusDecision;
import isv.sap.payment.dao.PaymentOrderDao;
import isv.sap.payment.model.IsvAlternativePaymentUpdateOrderStatusJobModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusContext;
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService;

import static de.hybris.platform.core.enums.OrderStatus.WAITING_FOR_PAYMENT;
import static de.hybris.platform.cronjob.enums.CronJobResult.SUCCESS;
import static de.hybris.platform.cronjob.enums.CronJobStatus.FINISHED;

/**
 * A job performable implementation that encapsulates update of alternative payment order status.
 */
public class UpdateAlternativePaymentOrderStatusJob
        extends AbstractAbortableJobPerformable<IsvAlternativePaymentUpdateOrderStatusJobModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(UpdateAlternativePaymentOrderStatusJob.class);

    @Resource(name = "isv.sap.payment.orderDao")
    private PaymentOrderDao orderDao;

    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusContext")
    private AlternativePaymentOrderStatusContext alternativePaymentOrderStatusContext;

    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusService")
    private AlternativePaymentOrderStatusService alternativePaymentOrderStatusService;

    @Resource
    private BusinessProcessService businessProcessService;

    /**
     * Retrieves all waiting for payment orders and checks current order status.
     * Whenever a different status is obtained, corresponding order is updated.
     *
     * @param jobModel holds the configuration for current execution
     * @return an instance of {@link PerformResult} that encapsulates status and result.
     */
    @Override
    public PerformResult perform(final IsvAlternativePaymentUpdateOrderStatusJobModel jobModel)
    {
        LOG.info("Started the check payment status cron job.");

        orderDao.findOrdersByStatus(WAITING_FOR_PAYMENT).forEach(order ->
        {
            final IsvPaymentTransactionModel transaction = alternativePaymentOrderStatusService
                    .getAlternativePaymentTransaction(order);
            final CheckStatusDecision checkStatusDecision = alternativePaymentOrderStatusService
                    .shouldRunCheckStatus(transaction);

            if (!checkStatusDecision.SKIP.equals(checkStatusDecision))
            {
                final OrderStatus status = order.getStatus();

                alternativePaymentOrderStatusContext
                        .getOrderHandler(checkStatusDecision, transaction.getAlternativePaymentMethod()).handle(order);

                if (status != order.getStatus())
                {
                    triggerEvent(order);
                }
            }
        });

        LOG.info("Finished the check payment status cron job");

        return new PerformResult(SUCCESS, FINISHED);
    }

    protected void triggerEvent(final OrderModel order)
    {
        order.getOrderProcess().stream().forEach(process ->
        {
            LOG.info("Triggering a new status update for order [{}] for business process [{}]", order.getGuid(),
                    process.getCode());

            businessProcessService.triggerEvent(process.getCode() + "_checkAlternativePaymentStatus");
        });
    }
}
