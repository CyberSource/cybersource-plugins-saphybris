package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderprocessing.events.PaymentFailedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.SendPaymentFailedNotificationAction;

@UnitTest
public class SendPaymentFailedNotificationTest
{
    @InjectMocks
    private final SendPaymentFailedNotificationAction sendPaymentFailedNotification = new SendPaymentFailedNotificationAction();

    @Mock
    private EventService eventService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link SendPaymentFailedNotificationAction#executeAction(OrderProcessModel)}
     */
    @Test
    public void testExecuteActionOrderProcessModel()
    {
        final OrderProcessModel process = new OrderProcessModel();
        sendPaymentFailedNotification.executeAction(process);

        Mockito.verify(eventService).publishEvent(Mockito.<PaymentFailedEvent>argThat(event ->
                event != null && event.getProcess().equals(process)
        ));
    }
}
