/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.addon.b2b.jalo;

import de.hybris.platform.jalo.GenericItem;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import isv.sap.payment.addon.b2b.constants.Isvb2bpaymentaddonConstants;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generated class for type {@link isv.sap.payment.addon.b2b.jalo.ReplenishmentInfo ReplenishmentInfo}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedReplenishmentInfo extends GenericItem
{
	/** Qualifier of the <code>ReplenishmentInfo.day</code> attribute **/
	public static final String DAY = "day";
	/** Qualifier of the <code>ReplenishmentInfo.daysOfWeek</code> attribute **/
	public static final String DAYSOFWEEK = "daysOfWeek";
	/** Qualifier of the <code>ReplenishmentInfo.dayOfMonth</code> attribute **/
	public static final String DAYOFMONTH = "dayOfMonth";
	/** Qualifier of the <code>ReplenishmentInfo.week</code> attribute **/
	public static final String WEEK = "week";
	/** Qualifier of the <code>ReplenishmentInfo.recurrence</code> attribute **/
	public static final String RECURRENCE = "recurrence";
	/** Qualifier of the <code>ReplenishmentInfo.startDate</code> attribute **/
	public static final String STARTDATE = "startDate";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>();
		tmp.put(DAY, AttributeMode.INITIAL);
		tmp.put(DAYSOFWEEK, AttributeMode.INITIAL);
		tmp.put(DAYOFMONTH, AttributeMode.INITIAL);
		tmp.put(WEEK, AttributeMode.INITIAL);
		tmp.put(RECURRENCE, AttributeMode.INITIAL);
		tmp.put(STARTDATE, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.day</code> attribute.
	 * @return the day
	 */
	public Integer getDay(final SessionContext ctx)
	{
		return (Integer)getProperty( ctx, DAY);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.day</code> attribute.
	 * @return the day
	 */
	public Integer getDay()
	{
		return getDay( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @return the day
	 */
	public int getDayAsPrimitive(final SessionContext ctx)
	{
		Integer value = getDay( ctx );
		return value != null ? value.intValue() : 0;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @return the day
	 */
	public int getDayAsPrimitive()
	{
		return getDayAsPrimitive( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @param value the day
	 */
	public void setDay(final SessionContext ctx, final Integer value)
	{
		setProperty(ctx, DAY,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @param value the day
	 */
	public void setDay(final Integer value)
	{
		setDay( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @param value the day
	 */
	public void setDay(final SessionContext ctx, final int value)
	{
		setDay( ctx,Integer.valueOf( value ) );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.day</code> attribute. 
	 * @param value the day
	 */
	public void setDay(final int value)
	{
		setDay( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute.
	 * @return the dayOfMonth
	 */
	public Integer getDayOfMonth(final SessionContext ctx)
	{
		return (Integer)getProperty( ctx, DAYOFMONTH);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute.
	 * @return the dayOfMonth
	 */
	public Integer getDayOfMonth()
	{
		return getDayOfMonth( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @return the dayOfMonth
	 */
	public int getDayOfMonthAsPrimitive(final SessionContext ctx)
	{
		Integer value = getDayOfMonth( ctx );
		return value != null ? value.intValue() : 0;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @return the dayOfMonth
	 */
	public int getDayOfMonthAsPrimitive()
	{
		return getDayOfMonthAsPrimitive( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @param value the dayOfMonth
	 */
	public void setDayOfMonth(final SessionContext ctx, final Integer value)
	{
		setProperty(ctx, DAYOFMONTH,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @param value the dayOfMonth
	 */
	public void setDayOfMonth(final Integer value)
	{
		setDayOfMonth( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @param value the dayOfMonth
	 */
	public void setDayOfMonth(final SessionContext ctx, final int value)
	{
		setDayOfMonth( ctx,Integer.valueOf( value ) );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.dayOfMonth</code> attribute. 
	 * @param value the dayOfMonth
	 */
	public void setDayOfMonth(final int value)
	{
		setDayOfMonth( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.daysOfWeek</code> attribute.
	 * @return the daysOfWeek
	 */
	public List<EnumerationValue> getDaysOfWeek(final SessionContext ctx)
	{
		List<EnumerationValue> coll = (List<EnumerationValue>)getProperty( ctx, DAYSOFWEEK);
		return coll != null ? coll : Collections.EMPTY_LIST;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.daysOfWeek</code> attribute.
	 * @return the daysOfWeek
	 */
	public List<EnumerationValue> getDaysOfWeek()
	{
		return getDaysOfWeek( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.daysOfWeek</code> attribute. 
	 * @param value the daysOfWeek
	 */
	public void setDaysOfWeek(final SessionContext ctx, final List<EnumerationValue> value)
	{
		setProperty(ctx, DAYSOFWEEK,value == null || !value.isEmpty() ? value : null );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.daysOfWeek</code> attribute. 
	 * @param value the daysOfWeek
	 */
	public void setDaysOfWeek(final List<EnumerationValue> value)
	{
		setDaysOfWeek( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.recurrence</code> attribute.
	 * @return the recurrence
	 */
	public EnumerationValue getRecurrence(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, RECURRENCE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.recurrence</code> attribute.
	 * @return the recurrence
	 */
	public EnumerationValue getRecurrence()
	{
		return getRecurrence( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.recurrence</code> attribute. 
	 * @param value the recurrence
	 */
	public void setRecurrence(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, RECURRENCE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.recurrence</code> attribute. 
	 * @param value the recurrence
	 */
	public void setRecurrence(final EnumerationValue value)
	{
		setRecurrence( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.startDate</code> attribute.
	 * @return the startDate
	 */
	public Date getStartDate(final SessionContext ctx)
	{
		return (Date)getProperty( ctx, STARTDATE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.startDate</code> attribute.
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return getStartDate( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.startDate</code> attribute. 
	 * @param value the startDate
	 */
	public void setStartDate(final SessionContext ctx, final Date value)
	{
		setProperty(ctx, STARTDATE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.startDate</code> attribute. 
	 * @param value the startDate
	 */
	public void setStartDate(final Date value)
	{
		setStartDate( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.week</code> attribute.
	 * @return the week
	 */
	public Integer getWeek(final SessionContext ctx)
	{
		return (Integer)getProperty( ctx, WEEK);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.week</code> attribute.
	 * @return the week
	 */
	public Integer getWeek()
	{
		return getWeek( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @return the week
	 */
	public int getWeekAsPrimitive(final SessionContext ctx)
	{
		Integer value = getWeek( ctx );
		return value != null ? value.intValue() : 0;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @return the week
	 */
	public int getWeekAsPrimitive()
	{
		return getWeekAsPrimitive( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @param value the week
	 */
	public void setWeek(final SessionContext ctx, final Integer value)
	{
		setProperty(ctx, WEEK,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @param value the week
	 */
	public void setWeek(final Integer value)
	{
		setWeek( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @param value the week
	 */
	public void setWeek(final SessionContext ctx, final int value)
	{
		setWeek( ctx,Integer.valueOf( value ) );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>ReplenishmentInfo.week</code> attribute. 
	 * @param value the week
	 */
	public void setWeek(final int value)
	{
		setWeek( getSession().getSessionContext(), value );
	}
	
}
