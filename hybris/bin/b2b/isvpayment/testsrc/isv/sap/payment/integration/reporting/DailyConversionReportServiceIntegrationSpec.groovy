package isv.sap.payment.integration.reporting

import javax.annotation.Resource

import de.hybris.bootstrap.annotations.ManualTest
import org.junit.Ignore
import org.junit.Test

import isv.cjl.payment.report.service.ConversionReportService
import isv.cjl.payment.report.service.ReportTimeService
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

@ManualTest
@Ignore('Test Merchant configuration should be setup and reports should be available in EBC portal, so ignoring by default')
class DailyConversionReportServiceIntegrationSpec extends IsvIntegrationSpec
{
    @Resource(name = 'isv.cjl.payment.report.service.onDemandConversionReportTimeService')
    ReportTimeService reportTimeService

    @Resource(name = 'isv.cjl.payment.report.service.conversionReportService')
    ConversionReportService conversionReportService

    Date lastRunDate = new Date() - 1
    Date startDate
    Date endDate

    def setup()
    {
        def reportInterval = reportTimeService.getReportInterval(lastRunDate)

        startDate = reportInterval.start.toDate()
        endDate = reportInterval.end.toDate()
    }

    @Test
    def 'should fetch daily conversion reports for all merchants'()
    {
        expect:
        conversionReportService.fetchDailyConversionReport(startDate)
    }

    @Test
    def 'should fetch on-demand conversion reports for all merchants'()
    {
        expect:
        conversionReportService.fetchOnDemandConversionReport(startDate, endDate)
    }
}
