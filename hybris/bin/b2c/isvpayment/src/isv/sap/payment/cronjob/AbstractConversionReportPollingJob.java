package isv.sap.payment.cronjob;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import isv.cjl.payment.model.ConversionReportInfo;
import isv.cjl.payment.report.service.ConversionReportService;
import isv.sap.payment.cronjob.data.ReportConversionResult;
import isv.sap.payment.model.IsvConversionReportPollingCronJobModel;
import isv.sap.payment.report.listener.ReportListener;

import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractConversionReportPollingJob
        extends AbstractAbortableJobPerformable<IsvConversionReportPollingCronJobModel>
{
    @Resource(name = "isv.cjl.payment.report.service.conversionReportService")
    private ConversionReportService conversionReportService;

    private List<ReportListener<ConversionReportInfo>> listeners = newArrayList();

    protected abstract ReportConversionResult getReportConversions(final Date lastRunDate);

    @Override
    public PerformResult perform(final IsvConversionReportPollingCronJobModel cronJob)
    {
        final ReportConversionResult conversionResult = getReportConversions(cronJob.getLastRunDate());

        listeners.forEach(listener -> listener.handle(conversionResult.getConversions()));

        cronJob.setLastRunDate(conversionResult.getNewLastRunDate());
        modelService.save(cronJob);

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public void setListeners(final List<ReportListener<ConversionReportInfo>> listeners)
    {
        this.listeners = listeners;
    }

    public ConversionReportService getConversionReportService()
    {
        return conversionReportService;
    }

    public void setConversionReportService(final ConversionReportService conversionReportService)
    {
        this.conversionReportService = conversionReportService;
    }
}
