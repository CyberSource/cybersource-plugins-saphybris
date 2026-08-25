package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderprocessing.events.AuthorizationFailedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.SendAuthorizationFailedNotificationAction;

@UnitTest
public class SendAuthorizationFailedNotificationTest
{
    @InjectMocks
    private final SendAuthorizationFailedNotificationAction sendAuthorizationFailedNotification = new SendAuthorizationFailedNotificationAction();

    @Mock
    private EventService eventService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link SendAuthorizationFailedNotificationAction#executeAction(OrderProcessModel)}
     */
    @Test
    public void testExecuteActionOrderProcessModel()
    {
        final OrderProcessModel process = new OrderProcessModel();
        sendAuthorizationFailedNotification.executeAction(process);

        Mockito.verify(eventService).publishEvent(Mockito.<AuthorizationFailedEvent>argThat(event ->
                event != null && event.getProcess().equals(process)
        ));
    }
}
