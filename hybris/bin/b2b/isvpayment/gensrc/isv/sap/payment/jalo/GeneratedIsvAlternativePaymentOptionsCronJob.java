/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.cronjob.jalo.CronJob;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.jalo.IsvMerchant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link de.hybris.platform.cronjob.jalo.CronJob IsvAlternativePaymentOptionsCronJob}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvAlternativePaymentOptionsCronJob extends CronJob
{
	/** Qualifier of the <code>IsvAlternativePaymentOptionsCronJob.merchant</code> attribute **/
	public static final String MERCHANT = "merchant";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>(CronJob.DEFAULT_INITIAL_ATTRIBUTES);
		tmp.put(MERCHANT, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvAlternativePaymentOptionsCronJob.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant(final SessionContext ctx)
	{
		return (IsvMerchant)getProperty( ctx, MERCHANT);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvAlternativePaymentOptionsCronJob.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant()
	{
		return getMerchant( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvAlternativePaymentOptionsCronJob.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final SessionContext ctx, final IsvMerchant value)
	{
		setProperty(ctx, MERCHANT,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvAlternativePaymentOptionsCronJob.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final IsvMerchant value)
	{
		setMerchant( getSession().getSessionContext(), value );
	}
	
}
