/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import de.hybris.platform.jalo.order.payment.PaymentMode;
import isv.sap.payment.constants.IsvPaymentConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link de.hybris.platform.jalo.order.payment.PaymentMode IsvPaymentMode}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvPaymentMode extends PaymentMode
{
	/** Qualifier of the <code>IsvPaymentMode.paymentType</code> attribute **/
	public static final String PAYMENTTYPE = "paymentType";
	/** Qualifier of the <code>IsvPaymentMode.paymentSubType</code> attribute **/
	public static final String PAYMENTSUBTYPE = "paymentSubType";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(PaymentMode.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(PAYMENTTYPE, AttributeMode.INITIAL);
		tmp.put(PAYMENTSUBTYPE, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentMode.paymentSubType</code> attribute.
	 * @return the paymentSubType - Concrete alternative payment type, e.g.: IDL, SOF, etc. for cases when main payment type is ALTERNATIVE_PAYMENT
	 */
	public EnumerationValue getPaymentSubType(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PAYMENTSUBTYPE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentMode.paymentSubType</code> attribute.
	 * @return the paymentSubType - Concrete alternative payment type, e.g.: IDL, SOF, etc. for cases when main payment type is ALTERNATIVE_PAYMENT
	 */
	public EnumerationValue getPaymentSubType()
	{
		return getPaymentSubType( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentMode.paymentSubType</code> attribute. 
	 * @param value the paymentSubType - Concrete alternative payment type, e.g.: IDL, SOF, etc. for cases when main payment type is ALTERNATIVE_PAYMENT
	 */
	public void setPaymentSubType(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PAYMENTSUBTYPE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentMode.paymentSubType</code> attribute. 
	 * @param value the paymentSubType - Concrete alternative payment type, e.g.: IDL, SOF, etc. for cases when main payment type is ALTERNATIVE_PAYMENT
	 */
	public void setPaymentSubType(final EnumerationValue value)
	{
		setPaymentSubType( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentMode.paymentType</code> attribute.
	 * @return the paymentType
	 */
	public EnumerationValue getPaymentType(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PAYMENTTYPE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentMode.paymentType</code> attribute.
	 * @return the paymentType
	 */
	public EnumerationValue getPaymentType()
	{
		return getPaymentType( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentMode.paymentType</code> attribute. 
	 * @param value the paymentType
	 */
	public void setPaymentType(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PAYMENTTYPE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentMode.paymentType</code> attribute. 
	 * @param value the paymentType
	 */
	public void setPaymentType(final EnumerationValue value)
	{
		setPaymentType( getSession().getSessionContext(), value );
	}
	
}
