package isv.sap.payment.cronjob

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.cronjob.enums.CronJobResult
import de.hybris.platform.cronjob.enums.CronJobStatus
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.processengine.BusinessProcessService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.CheckStatusDecision
import isv.sap.payment.dao.PaymentOrderDao
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.model.IsvAlternativePaymentUpdateOrderStatusJobModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusContext
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService
import isv.sap.payment.service.alternativepayment.handler.AlternativePaymentOrderStatusHandler

import static de.hybris.platform.core.enums.OrderStatus.COMPLETED
import static de.hybris.platform.core.enums.OrderStatus.WAITING_FOR_PAYMENT

@UnitTest
class UpdateAlternativePaymentOrderStatusJobSpec extends Specification
{
    def paymentOrderDao = Mock([useObjenesis: false], PaymentOrderDao)

    def alternativePaymentOrderStatusContext = Mock([useObjenesis: false], AlternativePaymentOrderStatusContext)

    def orderHandler = Mock([useObjenesis: false], AlternativePaymentOrderStatusHandler)

    def order = Mock([useObjenesis: false], OrderModel)

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    def alternativePaymentOrderStatusService = Mock([useObjenesis: false], AlternativePaymentOrderStatusService)

    def businessProcessService = Mock([useObjenesis: false], BusinessProcessService)

    def updateAlternativePaymentOrderStatusJob = new UpdateAlternativePaymentOrderStatusJob()

    void setup()
    {
        updateAlternativePaymentOrderStatusJob.orderDao = paymentOrderDao
        updateAlternativePaymentOrderStatusJob.alternativePaymentOrderStatusContext = alternativePaymentOrderStatusContext
        updateAlternativePaymentOrderStatusJob.businessProcessService = businessProcessService
        updateAlternativePaymentOrderStatusJob.alternativePaymentOrderStatusService = alternativePaymentOrderStatusService

        order.status >> WAITING_FOR_PAYMENT
        paymentOrderDao.findOrdersByStatus(WAITING_FOR_PAYMENT) >> [order]
        transaction.alternativePaymentMethod >> AlternativePaymentMethod.KLI
        order.orderProcess >> [Mock([useObjenesis: false], OrderProcessModel)]
        alternativePaymentOrderStatusService.getAlternativePaymentTransaction(order) >> transaction
    }

    @Test
    def 'run cron job and don\'t process the order when check status decision is SKIP'()
    {
        given:
        alternativePaymentOrderStatusService.shouldRunCheckStatus(transaction) >> CheckStatusDecision.SKIP

        when:
        def result = updateAlternativePaymentOrderStatusJob.perform(new IsvAlternativePaymentUpdateOrderStatusJobModel())

        then:
        result.result == CronJobResult.SUCCESS
        result.status == CronJobStatus.FINISHED

        and:
        0 * alternativePaymentOrderStatusContext.getOrderHandler(_, _) >> null
    }

    @Test
    def 'run cron job and process order when check status decision is RUN leaving the order in pending mode'()
    {
        given:
        alternativePaymentOrderStatusService.shouldRunCheckStatus(transaction) >> CheckStatusDecision.RUN
        alternativePaymentOrderStatusContext.getOrderHandler(CheckStatusDecision.RUN, transaction.alternativePaymentMethod) >> orderHandler

        when:
        def result = updateAlternativePaymentOrderStatusJob.perform(new IsvAlternativePaymentUpdateOrderStatusJobModel())

        then:
        result.result == CronJobResult.SUCCESS
        result.status == CronJobStatus.FINISHED

        and:
        1 * orderHandler.handle(order)
        0 * businessProcessService.triggerEvent(_)
    }

    @Test
    def 'run cron job and process order when check status decision is RUN triggering business process event'()
    {
        given:
        alternativePaymentOrderStatusService.shouldRunCheckStatus(transaction) >> CheckStatusDecision.RUN
        alternativePaymentOrderStatusContext.getOrderHandler(CheckStatusDecision.RUN, transaction.alternativePaymentMethod) >> orderHandler

        when:
        def result = updateAlternativePaymentOrderStatusJob.perform(new IsvAlternativePaymentUpdateOrderStatusJobModel())

        then:
        2 * order.status >> WAITING_FOR_PAYMENT >> COMPLETED
        result.result == CronJobResult.SUCCESS
        result.status == CronJobStatus.FINISHED

        and:
        1 * orderHandler.handle(order)
        1 * businessProcessService.triggerEvent(_)
    }

    @Test
    def 'run cron job when no orders waiting for payment found'()
    {
        when:
        updateAlternativePaymentOrderStatusJob.perform(new IsvAlternativePaymentUpdateOrderStatusJobModel())

        then:
        1 * paymentOrderDao.findOrdersByStatus(WAITING_FOR_PAYMENT) >> Collections.emptyList()
        0 * orderHandler.handle(_ as OrderModel)
    }
}
