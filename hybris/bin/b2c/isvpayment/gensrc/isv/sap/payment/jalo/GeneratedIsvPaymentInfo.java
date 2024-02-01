/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Nov 23, 2022, 6:51:10 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.payment.PaymentInfo;
import isv.sap.payment.constants.IsvPaymentConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link de.hybris.platform.jalo.order.payment.PaymentInfo IsvPaymentInfo}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvPaymentInfo extends PaymentInfo
{
	/** Qualifier of the <code>IsvPaymentInfo.properties</code> attribute **/
	public static final String PROPERTIES = "properties";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(PaymentInfo.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(PROPERTIES, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentInfo.properties</code> attribute.
	 * @return the properties
	 */
	public Map<String,String> getAllProperties(final SessionContext ctx)
	{
		Map<String,String> map = (Map<String,String>)getProperty( ctx, PROPERTIES);
		return map != null ? map : Collections.EMPTY_MAP;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentInfo.properties</code> attribute.
	 * @return the properties
	 */
	public Map<String,String> getAllProperties()
	{
		return getAllProperties( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentInfo.properties</code> attribute. 
	 * @param value the properties
	 */
	public void setAllProperties(final SessionContext ctx, final Map<String,String> value)
	{
		setProperty(ctx, PROPERTIES,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentInfo.properties</code> attribute. 
	 * @param value the properties
	 */
	public void setAllProperties(final Map<String,String> value)
	{
		setAllProperties( getSession().getSessionContext(), value );
	}
	
}
