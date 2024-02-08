package isv.sap.payment.cronjob;

import java.util.Collection;
import java.util.Date;

import com.google.common.collect.Lists;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.time.TimeService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import isv.cjl.payment.model.ConversionReportInfo;
import isv.cjl.payment.report.service.ConversionReportService;
import isv.cjl.payment.report.service.ReportTimeService;
import isv.sap.payment.cronjob.data.ReportConversionResult;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class OnDemandConversionReportPollingJobTest
{
    @Spy
    @InjectMocks
    private final OnDemandConversionReportPollingJob job = new OnDemandConversionReportPollingJob();

    @Mock
    private ReportTimeService reportTimeService;

    @Mock
    private ConversionReportService conversionReportService;

    @Mock
    private TimeService timeService;

    @Test
    public void shouldGetReportConversions()
    {
        final Date lastRunDate = DateTime.now().minusDays(1).toDate();
        final Collection<ConversionReportInfo> conversions = Lists.newArrayList();
        final Date currentTime = new Date();
        final Interval interval = new Interval(lastRunDate.getTime(), currentTime.getTime());

        when(timeService.getCurrentTime()).thenReturn(currentTime);
        when(reportTimeService.getReportInterval(lastRunDate)).thenReturn(interval);
        when(conversionReportService
                .fetchOnDemandConversionReport(interval.getStart().toDate(), interval.getEnd().toDate()))
                .thenReturn(conversions);

        final ReportConversionResult result = job.getReportConversions(lastRunDate);

        assertThat(result.getNewLastRunDate(), is(currentTime));
        assertThat(result.getConversions(), is(conversions));
    }
}