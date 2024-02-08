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
import de.hybris.platform.payment.jalo.PaymentTransaction;
import isv.sap.payment.constants.IsvPaymentConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link de.hybris.platform.payment.jalo.PaymentTransaction IsvPaymentTransaction}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvPaymentTransaction extends PaymentTransaction
{
	/** Qualifier of the <code>IsvPaymentTransaction.merchantId</code> attribute **/
	public static final String MERCHANTID = "merchantId";
	/** Qualifier of the <code>IsvPaymentTransaction.alternativePaymentMethod</code> attribute **/
	public static final String ALTERNATIVEPAYMENTMETHOD = "alternativePaymentMethod";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(PaymentTransaction.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(MERCHANTID, AttributeMode.INITIAL);
		tmp.put(ALTERNATIVEPAYMENTMETHOD, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentTransaction.alternativePaymentMethod</code> attribute.
	 * @return the alternativePaymentMethod
	 */
	public EnumerationValue getAlternativePaymentMethod(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, ALTERNATIVEPAYMENTMETHOD);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentTransaction.alternativePaymentMethod</code> attribute.
	 * @return the alternativePaymentMethod
	 */
	public EnumerationValue getAlternativePaymentMethod()
	{
		return getAlternativePaymentMethod( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentTransaction.alternativePaymentMethod</code> attribute. 
	 * @param value the alternativePaymentMethod
	 */
	public void setAlternativePaymentMethod(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, ALTERNATIVEPAYMENTMETHOD,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentTransaction.alternativePaymentMethod</code> attribute. 
	 * @param value the alternativePaymentMethod
	 */
	public void setAlternativePaymentMethod(final EnumerationValue value)
	{
		setAlternativePaymentMethod( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentTransaction.merchantId</code> attribute.
	 * @return the merchantId
	 */
	public String getMerchantId(final SessionContext ctx)
	{
		return (String)getProperty( ctx, MERCHANTID);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvPaymentTransaction.merchantId</code> attribute.
	 * @return the merchantId
	 */
	public String getMerchantId()
	{
		return getMerchantId( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentTransaction.merchantId</code> attribute. 
	 * @param value the merchantId
	 */
	public void setMerchantId(final SessionContext ctx, final String value)
	{
		setProperty(ctx, MERCHANTID,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvPaymentTransaction.merchantId</code> attribute. 
	 * @param value the merchantId
	 */
	public void setMerchantId(final String value)
	{
		setMerchantId( getSession().getSessionContext(), value );
	}
	
}
