package isv.sap.payment.report.listener.conversion

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.processengine.BusinessProcessService
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.model.ConversionReportInfo
import isv.sap.payment.dao.PaymentOrderDao
import isv.sap.payment.report.listener.conversion.decision.DecisionChangeStrategy

@UnitTest
class ProcessConversionReportListenerSpec extends Specification
{
    def conversionReportInfo = Mock(ConversionReportInfo)

    def order = Mock(OrderModel)

    def orderDao = Mock(PaymentOrderDao)

    def businessProcessService = Mock(BusinessProcessService)

    def decisionStrategy = Mock(DecisionChangeStrategy)

    def handler = new ProcessConversionReportListener()

    void setup()
    {
        handler.orderDao = orderDao
        handler.businessProcessService = businessProcessService
        handler.decisionStrategies = [decisionStrategy]

        conversionReportInfo.merchantReferenceCode >> '1234567890'
        conversionReportInfo.originalDecision >> 'review'
        conversionReportInfo.newDecision >> 'reject'

        decisionStrategy.supports('REVIEWREJECT') >> true

        order.guid >> '1234567890'
        order.code >> '0987654321'
    }

    @Test
    def 'handler should process conversion report'()
    {
        given:
        orderDao.findOrderByGuid('1234567890') >> order
        order.status >> OrderStatus.FRAUD_DECISION

        when:
        handler.handle([conversionReportInfo])

        then:
        1 * decisionStrategy.updateOrderFraudStatus(order)
        1 * businessProcessService.triggerEvent('0987654321_ReviewDecision')
    }

    @Test
    def 'handler should skip already processed report'()
    {
        given:
        orderDao.findOrderByGuid('1234567890') >> order
        order.status >> OrderStatus.COMPLETED

        when:
        handler.handle([conversionReportInfo])

        then:
        0 * decisionStrategy.updateOrderFraudStatus(_, _)
        0 * businessProcessService.triggerEvent(_)
    }

    @Test
    def 'handler should skip null orders'()
    {
        given:
        orderDao.findOrderByGuid('1234567890') >> null

        when:
        handler.handle([conversionReportInfo])

        then:
        0 * decisionStrategy.updateOrderFraudStatus(_, _)
        0 * businessProcessService.triggerEvent(_)
    }

    @Test
    def 'handler should skip conversion when an exception occurs'()
    {
        given:
        orderDao.findOrderByGuid('1234567890') >> { throw new UnknownIdentifierException('Order not found') }

        when:
        handler.handle([conversionReportInfo])

        then:
        0 * decisionStrategy.updateOrderFraudStatus(_, _)
        0 * businessProcessService.triggerEvent(_)
    }
}
