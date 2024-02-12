/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Nov 23, 2022, 6:51:10 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.jalo.GenericItem;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import isv.sap.payment.constants.IsvPaymentConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link isv.sap.payment.jalo.IsvCheckAlternativePaymentStatusConfiguration IsvCheckAlternativePaymentStatusConfiguration}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvCheckAlternativePaymentStatusConfiguration extends GenericItem
{
	/** Qualifier of the <code>IsvCheckAlternativePaymentStatusConfiguration.paymentMethod</code> attribute **/
	public static final String PAYMENTMETHOD = "paymentMethod";
	/** Qualifier of the <code>IsvCheckAlternativePaymentStatusConfiguration.statusMap</code> attribute **/
	public static final String STATUSMAP = "statusMap";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>();
		tmp.put(PAYMENTMETHOD, AttributeMode.INITIAL);
		tmp.put(STATUSMAP, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvCheckAlternativePaymentStatusConfiguration.paymentMethod</code> attribute.
	 * @return the paymentMethod
	 */
	public EnumerationValue getPaymentMethod(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PAYMENTMETHOD);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvCheckAlternativePaymentStatusConfiguration.paymentMethod</code> attribute.
	 * @return the paymentMethod
	 */
	public EnumerationValue getPaymentMethod()
	{
		return getPaymentMethod( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvCheckAlternativePaymentStatusConfiguration.paymentMethod</code> attribute. 
	 * @param value the paymentMethod
	 */
	public void setPaymentMethod(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PAYMENTMETHOD,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvCheckAlternativePaymentStatusConfiguration.paymentMethod</code> attribute. 
	 * @param value the paymentMethod
	 */
	public void setPaymentMethod(final EnumerationValue value)
	{
		setPaymentMethod( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvCheckAlternativePaymentStatusConfiguration.statusMap</code> attribute.
	 * @return the statusMap
	 */
	public Map<EnumerationValue,EnumerationValue> getAllStatusMap(final SessionContext ctx)
	{
		Map<EnumerationValue,EnumerationValue> map = (Map<EnumerationValue,EnumerationValue>)getProperty( ctx, STATUSMAP);
		return map != null ? map : Collections.EMPTY_MAP;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvCheckAlternativePaymentStatusConfiguration.statusMap</code> attribute.
	 * @return the statusMap
	 */
	public Map<EnumerationValue,EnumerationValue> getAllStatusMap()
	{
		return getAllStatusMap( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvCheckAlternativePaymentStatusConfiguration.statusMap</code> attribute. 
	 * @param value the statusMap
	 */
	public void setAllStatusMap(final SessionContext ctx, final Map<EnumerationValue,EnumerationValue> value)
	{
		setProperty(ctx, STATUSMAP,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvCheckAlternativePaymentStatusConfiguration.statusMap</code> attribute. 
	 * @param value the statusMap
	 */
	public void setAllStatusMap(final Map<EnumerationValue,EnumerationValue> value)
	{
		setAllStatusMap( getSession().getSessionContext(), value );
	}
	
}
