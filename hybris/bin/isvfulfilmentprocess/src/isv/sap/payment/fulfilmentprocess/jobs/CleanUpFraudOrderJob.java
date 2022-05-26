package isv.sap.payment.fulfilmentprocess.jobs;

import java.util.List;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.sap.payment.cronjob.AbstractAbortableJobPerformable;
import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

/**
 * CronJob periodically send CleanUpEvent for <b>order-process</b> processes which are in action <b>waitForCleanUp</b>
 */
public class CleanUpFraudOrderJob extends AbstractAbortableJobPerformable<CronJobModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(CleanUpFraudOrderJob.class);

    private BusinessProcessService businessProcessService;

    @Override
    public PerformResult perform(final CronJobModel cronJob)
    {
        final String processDefinitionName = IsvfulfilmentprocessConstants.ORDER_PROCESS_NAME;
        final String processCurrentAction = "waitForCleanUp";
        final List<BusinessProcessModel> processes = getAllProcessByDefinitionAndCurrentAction(processDefinitionName,
                processCurrentAction);

        final String eventNameSuffix = "_CleanUpEvent";
        for (final BusinessProcessModel bpm : processes)
        {
            final String eventName = bpm.getCode() + eventNameSuffix;
            getBusinessProcessService().triggerEvent(eventName);
        }

        LOG.info("Cleanup fraud order job finished.");

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    protected List<BusinessProcessModel> getAllProcessByDefinitionAndCurrentAction(final String processDefinitionName,
            final String processCurrentAction)
    {
        final String query =
                "select {bp.PK} " + "from {BusinessProcess AS bp  JOIN ProcessTask AS pt ON {bp.pk} = {pt.process} } "
                        + "WHERE {bp.processDefinitionName} = ?processDefinitionName and {pt.action} = ?processCurrentAction";

        final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query);
        searchQuery.addQueryParameter("processDefinitionName", processDefinitionName);
        searchQuery.addQueryParameter("processCurrentAction", processCurrentAction);
        final SearchResult<BusinessProcessModel> processes = flexibleSearchService.search(searchQuery);
        return processes.getResult();
    }

    protected BusinessProcessService getBusinessProcessService()
    {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService)
    {
        this.businessProcessService = businessProcessService;
    }
}
