/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Nov 23, 2022, 6:51:10 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.cronjob.jalo.CronJob;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import isv.sap.payment.constants.IsvPaymentConstants;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link de.hybris.platform.cronjob.jalo.CronJob IsvConversionReportPollingCronJob}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvConversionReportPollingCronJob extends CronJob
{
	/** Qualifier of the <code>IsvConversionReportPollingCronJob.lastRunDate</code> attribute **/
	public static final String LASTRUNDATE = "lastRunDate";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(CronJob.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(LASTRUNDATE, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvConversionReportPollingCronJob.lastRunDate</code> attribute.
	 * @return the lastRunDate
	 */
	public Date getLastRunDate(final SessionContext ctx)
	{
		return (Date)getProperty( ctx, LASTRUNDATE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvConversionReportPollingCronJob.lastRunDate</code> attribute.
	 * @return the lastRunDate
	 */
	public Date getLastRunDate()
	{
		return getLastRunDate( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvConversionReportPollingCronJob.lastRunDate</code> attribute. 
	 * @param value the lastRunDate
	 */
	public void setLastRunDate(final SessionContext ctx, final Date value)
	{
		setProperty(ctx, LASTRUNDATE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvConversionReportPollingCronJob.lastRunDate</code> attribute. 
	 * @param value the lastRunDate
	 */
	public void setLastRunDate(final Date value)
	{
		setLastRunDate( getSession().getSessionContext(), value );
	}
	
}
