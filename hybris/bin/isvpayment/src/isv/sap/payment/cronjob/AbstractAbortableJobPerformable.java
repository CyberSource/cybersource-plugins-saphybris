package isv.sap.payment.cronjob;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;

public abstract class AbstractAbortableJobPerformable<T extends CronJobModel> extends AbstractJobPerformable<T>
{
    /**
     * Custom cron jobs operations are abortable.
     *
     * @return
     */
    @Override
    public boolean isAbortable()
    {
        return true;
    }
}
