package isv.sap.payment.cronjob.data;

import java.util.Collection;
import java.util.Date;

import isv.cjl.payment.model.ConversionReportInfo;

public class ReportConversionResult
{
    private final Date newLastRunDate;

    private final Collection<ConversionReportInfo> conversions;

    public ReportConversionResult(final Date newLastRunDate, final Collection<ConversionReportInfo> conversions)
    {
        this.conversions = conversions;
        this.newLastRunDate = newLastRunDate;
    }

    public Date getNewLastRunDate()
    {
        return newLastRunDate;
    }

    public Collection<ConversionReportInfo> getConversions()
    {
        return conversions;
    }
}
