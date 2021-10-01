package isv.sap.payment.fulfilmentprocess.test.actions.consignmentfulfilment;

import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.log4j.Logger;

import isv.sap.payment.fulfilmentprocess.test.actions.TestActionTemp;

public abstract class AbstractTestConsActionTemp extends TestActionTemp
{
    private static final Logger LOG = Logger.getLogger(AbstractTestConsActionTemp.class);

    @Override
    public String execute(final BusinessProcessModel process) throws Exception //NOPMD
    {
        LOG.info(getResult());
        return getResult();
    }
}
