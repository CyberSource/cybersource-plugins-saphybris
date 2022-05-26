package isv.sap.payment.fulfilmentprocess.test.actions.consignmentfulfilment;

import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.fulfilmentprocess.test.actions.TestActionTemp;

public abstract class AbstractTestConsActionTemp extends TestActionTemp
{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestConsActionTemp.class);

    @Override
    public String execute(final BusinessProcessModel process) throws Exception //NOPMD
    {
        LOG.info(getResult());
        return getResult();
    }
}
