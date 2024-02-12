package isv.sap.payment.fulfilmentprocess.test.events;

import java.util.concurrent.atomic.AtomicInteger;

import de.hybris.platform.servicelayer.event.events.AbstractEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

/**
 * Event Listener which only count number of onEvent() invocations
 *
 * @param <T>
 */
public class TestEventListenerCountingEvents<T extends AbstractEvent> extends AbstractEventListener<T>
{
    private final AtomicInteger numberOfEvents = new AtomicInteger(0);

    @Override
    protected void onEvent(final T event)
    {
        numberOfEvents.incrementAndGet();
    }

    /**
     * @return number of onEvent invocation since beginning or last resetCounter() invocation
     */
    public int getNumberOfEvents()
    {
        return numberOfEvents.get();
    }

    /**
     * reset counter which store number of onEvent() invocations
     */
    public void resetCounter()
    {
        numberOfEvents.set(0);
    }
}
