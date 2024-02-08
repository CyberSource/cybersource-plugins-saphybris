package isv.sap.payment.report.listener;

import java.util.Collection;

/**
 * Defines report listener component that processes a report.
 */
public interface ReportListener<T>
{
    void handle(final Collection<T> conversions);
}
