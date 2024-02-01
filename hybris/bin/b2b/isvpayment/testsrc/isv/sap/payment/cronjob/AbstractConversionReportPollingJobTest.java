package isv.sap.payment.cronjob;

import java.util.Date;

import com.google.common.collect.Lists;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import isv.cjl.payment.model.ConversionReportInfo;
import isv.sap.payment.cronjob.data.ReportConversionResult;
import isv.sap.payment.model.IsvConversionReportPollingCronJobModel;
import isv.sap.payment.report.listener.ReportListener;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class AbstractConversionReportPollingJobTest
{
    @Spy
    @InjectMocks
    private final AbstractConversionReportPollingJob job = new DailyConversionReportPollingJob();

    @Mock
    private ReportListener reportListener;

    @Mock
    private IsvConversionReportPollingCronJobModel cronJob;

    @Mock
    private ReportConversionResult reportConversionResult;

    @Mock
    private ConversionReportInfo conversionReportInfo;

    @Mock
    private ModelService modelService;

    @Test
    public void shouldPerformConversionReportJob()
    {
        final Date lastRunDate = new Date();
        final Date newLastRunDate = DateTime.now().plusHours(5).toDate();

        when(cronJob.getLastRunDate()).thenReturn(lastRunDate);
        when(reportConversionResult.getConversions()).thenReturn(Lists.newArrayList(conversionReportInfo));
        when(reportConversionResult.getNewLastRunDate()).thenReturn(newLastRunDate);
        doReturn(reportConversionResult).when(job).getReportConversions(lastRunDate);
        doNothing().when(reportListener).handle(Lists.newArrayList(conversionReportInfo));

        final PerformResult result = job.perform(cronJob);

        verify(cronJob).setLastRunDate(newLastRunDate);
        verify(modelService).save(cronJob);

        assertThat(result.getResult(), is(CronJobResult.SUCCESS));
        assertThat(result.getStatus(), is(CronJobStatus.FINISHED));
    }
}