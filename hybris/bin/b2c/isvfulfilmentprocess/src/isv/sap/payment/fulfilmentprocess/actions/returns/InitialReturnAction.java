package isv.sap.payment.fulfilmentprocess.actions.returns;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.basecommerce.enums.ReturnAction;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check whether the return request is an instore or an online request and redirects it to the appropriate step.
 */
public class InitialReturnAction extends AbstractAction<ReturnProcessModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(InitialReturnAction.class);

    @Override
    public String execute(final ReturnProcessModel process)
    {
        LOG.info("Process: {} in step {}", process.getCode(), getClass());

        final ReturnRequestModel returnRequest = process.getReturnRequest();

        final String transition = returnRequest.getReturnEntries().stream().allMatch(entry -> entry.getAction().equals(
                ReturnAction.IMMEDIATE)) ? Transition.INSTORE.toString() : Transition.ONLINE.toString();

        LOG.debug("Process: {} transitions to {}", process.getCode(), transition);

        return transition;
    }

    @Override
    public Set<String> getTransitions()
    {
        return Transition.getStringValues();
    }

    protected enum Transition
    {
        ONLINE, INSTORE;

        public static Set<String> getStringValues()
        {
            final Set<String> res = new HashSet<>();

            for (final Transition transition : Transition.values())
            {
                res.add(transition.toString());
            }
            return res;
        }
    }
}
