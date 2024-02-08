package isv.sap.payment.cronjob;

import java.util.Date;
import javax.annotation.Resource;

import de.hybris.platform.servicelayer.time.TimeService;
import org.joda.time.Interval;

import isv.cjl.payment.report.service.ReportTimeService;
import isv.sap.payment.cronjob.data.ReportConversionResult;

/**
 * This class is responsible for loading on demand conversion reports and notify subscribed listeners
 */
public class OnDemandConversionReportPollingJob extends AbstractConversionReportPollingJob
{
    @Resource
    private TimeService timeService;

    @Resource(name = "isv.cjl.payment.report.service.onDemandConversionReportTimeService")
    private ReportTimeService reportTimeService;

    @Override
    protected ReportConversionResult getReportConversions(final Date lastRunDate)
    {
        final Interval reportInterval = reportTimeService.getReportInterval(lastRunDate);

        return new ReportConversionResult(timeService.getCurrentTime(),
                getConversionReportService().fetchOnDemandConversionReport(
                        reportInterval.getStart().toDate(), reportInterval.getEnd().toDate()));
    }
}
