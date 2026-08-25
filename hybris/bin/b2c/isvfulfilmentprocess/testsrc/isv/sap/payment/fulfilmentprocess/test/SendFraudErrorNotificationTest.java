package isv.sap.payment.fulfilmentprocess.test;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderprocessing.events.FraudErrorEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import isv.sap.payment.fulfilmentprocess.actions.order.SendFraudErrorNotificationAction;

@UnitTest
public class SendFraudErrorNotificationTest
{
    @InjectMocks
    private final SendFraudErrorNotificationAction sendFraudErrorNotification = new SendFraudErrorNotificationAction();

    @Mock
    private EventService eventService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link SendFraudErrorNotificationAction#executeAction(OrderProcessModel)}
     */
    @Test
    public void testExecuteActionOrderProcessModel()
    {
        final OrderProcessModel process = new OrderProcessModel();
        sendFraudErrorNotification.executeAction(process);

        Mockito.verify(eventService).publishEvent(Mockito.<FraudErrorEvent>argThat(event ->
                event != null && event.getProcess().equals(process)
        ));
    }
}
