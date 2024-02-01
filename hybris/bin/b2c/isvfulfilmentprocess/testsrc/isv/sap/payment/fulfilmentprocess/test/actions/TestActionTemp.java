package isv.sap.payment.fulfilmentprocess.test.actions;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.core.Registry;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestActionTemp<T extends BusinessProcessModel> extends AbstractAction<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(TestActionTemp.class);

    private String result = "OK";

    private boolean throwException = false;

    public String getResult()
    {
        return result;
    }

    public void setResult(final String result)
    {
        this.result = result;
    }

    public void setThrowException(final boolean throwException)
    {
        this.throwException = throwException;
    }

    @Override
    public String execute(final T process) throws Exception
    {
        if (throwException)
        {
            throw new IllegalStateException("Error");
        }

        LOG.info(result);
        return result;
    }

    @Override
    public Set<String> getTransitions()
    {
        final Set<String> res = new HashSet<String>();
        res.add(result);
        return res;
    }

    protected BusinessProcessService getBusinessProcessService()
    {
        return (BusinessProcessService) Registry.getApplicationContext().getBean("businessProcessService");
    }
}
