package isv.sap.payment.cronjob;

import java.util.Date;
import javax.annotation.Resource;

import org.joda.time.Interval;

import isv.cjl.payment.report.service.ReportTimeService;
import isv.sap.payment.cronjob.data.ReportConversionResult;

/**
 * This class is responsible for loading daily conversion reports and notify subscribed listeners
 */
public class DailyConversionReportPollingJob extends AbstractConversionReportPollingJob
{
    @Resource(name = "isv.cjl.payment.report.service.dailyConversionReportTimeService")
    private ReportTimeService reportTimeService;

    @Override
    protected ReportConversionResult getReportConversions(final Date lastRunDate)
    {
        final Interval reportInterval = reportTimeService.getReportInterval(lastRunDate);
        final Date startDate = reportInterval.getStart().toDate();

        return new ReportConversionResult(startDate, getConversionReportService().fetchDailyConversionReport(startDate));
    }
}
