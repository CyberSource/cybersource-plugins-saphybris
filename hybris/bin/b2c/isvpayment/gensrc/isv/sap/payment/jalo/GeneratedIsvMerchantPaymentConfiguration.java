/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Nov 23, 2022, 6:51:10 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.basecommerce.jalo.site.BaseSite;
import de.hybris.platform.jalo.GenericItem;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.jalo.IsvMerchant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link isv.sap.payment.jalo.IsvMerchantPaymentConfiguration IsvMerchantPaymentConfiguration}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvMerchantPaymentConfiguration extends GenericItem
{
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.merchant</code> attribute **/
	public static final String MERCHANT = "merchant";
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.site</code> attribute **/
	public static final String SITE = "site";
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.paymentType</code> attribute **/
	public static final String PAYMENTTYPE = "paymentType";
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.currency</code> attribute **/
	public static final String CURRENCY = "currency";
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.paymentChannel</code> attribute **/
	public static final String PAYMENTCHANNEL = "paymentChannel";
	/** Qualifier of the <code>IsvMerchantPaymentConfiguration.authServiceCommerceIndicator</code> attribute **/
	public static final String AUTHSERVICECOMMERCEINDICATOR = "authServiceCommerceIndicator";
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>();
		tmp.put(MERCHANT, AttributeMode.INITIAL);
		tmp.put(SITE, AttributeMode.INITIAL);
		tmp.put(PAYMENTTYPE, AttributeMode.INITIAL);
		tmp.put(CURRENCY, AttributeMode.INITIAL);
		tmp.put(PAYMENTCHANNEL, AttributeMode.INITIAL);
		tmp.put(AUTHSERVICECOMMERCEINDICATOR, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.authServiceCommerceIndicator</code> attribute.
	 * @return the authServiceCommerceIndicator
	 */
	public String getAuthServiceCommerceIndicator(final SessionContext ctx)
	{
		return (String)getProperty( ctx, AUTHSERVICECOMMERCEINDICATOR);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.authServiceCommerceIndicator</code> attribute.
	 * @return the authServiceCommerceIndicator
	 */
	public String getAuthServiceCommerceIndicator()
	{
		return getAuthServiceCommerceIndicator( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.authServiceCommerceIndicator</code> attribute. 
	 * @param value the authServiceCommerceIndicator
	 */
	public void setAuthServiceCommerceIndicator(final SessionContext ctx, final String value)
	{
		setProperty(ctx, AUTHSERVICECOMMERCEINDICATOR,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.authServiceCommerceIndicator</code> attribute. 
	 * @param value the authServiceCommerceIndicator
	 */
	public void setAuthServiceCommerceIndicator(final String value)
	{
		setAuthServiceCommerceIndicator( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.currency</code> attribute.
	 * @return the currency
	 */
	public Currency getCurrency(final SessionContext ctx)
	{
		return (Currency)getProperty( ctx, CURRENCY);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.currency</code> attribute.
	 * @return the currency
	 */
	public Currency getCurrency()
	{
		return getCurrency( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.currency</code> attribute. 
	 * @param value the currency
	 */
	public void setCurrency(final SessionContext ctx, final Currency value)
	{
		setProperty(ctx, CURRENCY,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.currency</code> attribute. 
	 * @param value the currency
	 */
	public void setCurrency(final Currency value)
	{
		setCurrency( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant(final SessionContext ctx)
	{
		return (IsvMerchant)getProperty( ctx, MERCHANT);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant()
	{
		return getMerchant( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final SessionContext ctx, final IsvMerchant value)
	{
		setProperty(ctx, MERCHANT,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final IsvMerchant value)
	{
		setMerchant( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.paymentChannel</code> attribute.
	 * @return the paymentChannel
	 */
	public EnumerationValue getPaymentChannel(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PAYMENTCHANNEL);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.paymentChannel</code> attribute.
	 * @return the paymentChannel
	 */
	public EnumerationValue getPaymentChannel()
	{
		return getPaymentChannel( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.paymentChannel</code> attribute. 
	 * @param value the paymentChannel
	 */
	public void setPaymentChannel(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PAYMENTCHANNEL,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.paymentChannel</code> attribute. 
	 * @param value the paymentChannel
	 */
	public void setPaymentChannel(final EnumerationValue value)
	{
		setPaymentChannel( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.paymentType</code> attribute.
	 * @return the paymentType
	 */
	public EnumerationValue getPaymentType(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PAYMENTTYPE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.paymentType</code> attribute.
	 * @return the paymentType
	 */
	public EnumerationValue getPaymentType()
	{
		return getPaymentType( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.paymentType</code> attribute. 
	 * @param value the paymentType
	 */
	public void setPaymentType(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PAYMENTTYPE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.paymentType</code> attribute. 
	 * @param value the paymentType
	 */
	public void setPaymentType(final EnumerationValue value)
	{
		setPaymentType( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.site</code> attribute.
	 * @return the site
	 */
	public BaseSite getSite(final SessionContext ctx)
	{
		return (BaseSite)getProperty( ctx, SITE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantPaymentConfiguration.site</code> attribute.
	 * @return the site
	 */
	public BaseSite getSite()
	{
		return getSite( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.site</code> attribute. 
	 * @param value the site
	 */
	public void setSite(final SessionContext ctx, final BaseSite value)
	{
		setProperty(ctx, SITE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantPaymentConfiguration.site</code> attribute. 
	 * @param value the site
	 */
	public void setSite(final BaseSite value)
	{
		setSite( getSession().getSessionContext(), value );
	}
	
}
