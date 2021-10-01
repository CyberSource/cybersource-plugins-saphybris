package isv.sap.payment.cronjob;

import java.util.Collection;
import java.util.Date;

import com.google.common.collect.Lists;
import de.hybris.bootstrap.annotations.UnitTest;
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
public class DailyConversionReportPollingJobTest
{
    @Spy
    @InjectMocks
    private final DailyConversionReportPollingJob job = new DailyConversionReportPollingJob();

    @Mock
    private ReportTimeService reportTimeService;

    @Mock
    private ConversionReportService conversionReportService;

    @Test
    public void shouldGetReportConversions()
    {
        final Date lastRunDate = new Date();
        final Collection<ConversionReportInfo> conversions = Lists.newArrayList();

        when(reportTimeService.getReportInterval(lastRunDate))
                .thenReturn(new Interval(lastRunDate.getTime(), lastRunDate.getTime()));
        when(conversionReportService.fetchDailyConversionReport(lastRunDate)).thenReturn(conversions);

        final ReportConversionResult result = job.getReportConversions(lastRunDate);

        assertThat(result.getNewLastRunDate(), is(lastRunDate));
        assertThat(result.getConversions(), is(conversions));
    }
}