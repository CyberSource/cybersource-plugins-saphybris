package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.PrepareOrderForManualCheckAction;

@UnitTest
public class PrepareOrderForManualCheckTest
{
    private PrepareOrderForManualCheckAction prepareOrderForManualCheck;

    @Mock
    private ModelService modelService;

    @Mock
    private EventService eventService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        prepareOrderForManualCheck = new PrepareOrderForManualCheckAction();
        prepareOrderForManualCheck.setModelService(modelService);
        prepareOrderForManualCheck.setEventService(eventService);
    }

    @Test
    public void testExecute() throws Exception
    {

        final OrderProcessModel orderProcess = new OrderProcessModel();
        final OrderModel order = new OrderModel();
        order.setStatus(OrderStatus.CREATED);
        orderProcess.setOrder(order);
        prepareOrderForManualCheck.executeAction(orderProcess);
        Assert.assertEquals(OrderStatus.WAIT_FRAUD_MANUAL_CHECK, orderProcess.getOrder().getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteNullProcess() throws Exception
    {

        prepareOrderForManualCheck.executeAction(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteNullOrder() throws Exception
    {

        prepareOrderForManualCheck.executeAction(new OrderProcessModel());
    }
}
