package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderprocessing.events.OrderCompletedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.SendOrderCompletedNotificationAction;

@UnitTest
public class SendOrderCompletedNotificationTest
{
    @InjectMocks
    private final SendOrderCompletedNotificationAction sendOrderCompletedNotification = new SendOrderCompletedNotificationAction();

    @Mock
    private EventService eventService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link SendOrderCompletedNotificationAction#executeAction(OrderProcessModel)}
     */
    @Test
    public void testExecuteActionOrderProcessModel()
    {
        final OrderProcessModel process = new OrderProcessModel();
        sendOrderCompletedNotification.executeAction(process);

        Mockito.verify(eventService).publishEvent(Mockito.<OrderCompletedEvent>argThat(event ->
                event != null && event.getProcess().equals(process)
        ));
    }
}
