package isv.sap.payment.fulfilmentprocess.test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.time.TimeService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import isv.sap.payment.fulfilmentprocess.actions.order.ScheduleForCleanUpAction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
public class ScheduleForCleanUpActionTest
{
    private final Integer minPeriodWaitingForCleanUp = Integer.valueOf(10);

    private ScheduleForCleanUpAction action = null;

    private OrderProcessModel orderProcess = null;

    @Before
    public void setup()
    {
        prepareAction();
        prepareOrderProcess();
    }

    protected void prepareAction()
    {
        action = new ScheduleForCleanUpAction();
        action.setTimeService(mockTimeService());
        action.setMinPeriodWaitingForCleanUpInSeconds(minPeriodWaitingForCleanUp);
    }

    protected void prepareOrderProcess()
    {
        final OrderModel order = new OrderModel();
        order.setFraudReports(new HashSet<>());
        orderProcess = new OrderProcessModel();
        orderProcess.setOrder(order);
    }

    protected TimeService mockTimeService()
    {
        final TimeService mockedTimeService = mock(TimeService.class);
        when(mockedTimeService.getCurrentTime()).thenReturn(new Date());
        return mockedTimeService;
    }

    @Test
    public void testOrderShouldBeCleaned()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, -minPeriodWaitingForCleanUp.intValue() * 5);
        final Date timestamp = calendar.getTime();
        final FraudReportModel fraudReport = new FraudReportModel();
        fraudReport.setTimestamp(timestamp);
        fraudReport.setStatus(FraudStatus.FRAUD);
        orderProcess.getOrder().getFraudReports().add(fraudReport);

        final AbstractSimpleDecisionAction.Transition result = action.executeAction(orderProcess);

        Assert.assertEquals(AbstractSimpleDecisionAction.Transition.OK, result);
    }

    @Test
    public void testOrderShouldNotBeCleaned()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, (int) (minPeriodWaitingForCleanUp.intValue() * 0.5));
        final Date timestamp = calendar.getTime();
        final FraudReportModel fraudReport = new FraudReportModel();
        fraudReport.setTimestamp(timestamp);
        fraudReport.setStatus(FraudStatus.FRAUD);
        orderProcess.getOrder().getFraudReports().add(fraudReport);

        final AbstractSimpleDecisionAction.Transition result = action.executeAction(orderProcess);

        Assert.assertEquals(AbstractSimpleDecisionAction.Transition.NOK, result);
    }
}
