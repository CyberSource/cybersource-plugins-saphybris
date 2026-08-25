package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.events.OrderFraudCustomerNotificationEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.NotifyCustomerAboutFraudAction;

@UnitTest
public class SendOrderFraudCustomerNotificationEventTest
{
    @InjectMocks
    private final NotifyCustomerAboutFraudAction action = new NotifyCustomerAboutFraudAction();

    @Mock
    private EventService eventService;

    @Mock
    private ModelService modelService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteAction()
    {
        final OrderProcessModel process = new OrderProcessModel();
        final OrderModel order = new OrderModel();
        process.setOrder(order);
        action.executeAction(process);
        Mockito.verify(eventService).publishEvent(Mockito.<OrderFraudCustomerNotificationEvent>argThat(event ->
                event != null && event.getProcess().equals(process)
        ));

        Mockito.verify(modelService).save(order);
        Assert.assertEquals(OrderStatus.SUSPENDED, order.getStatus());
    }
}
