/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.jalo.GenericItem;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import de.hybris.platform.jalo.type.CollectionType;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.util.BidirectionalOneToManyHandler;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.jalo.IsvMerchant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link isv.sap.payment.jalo.IsvMerchantProfile IsvMerchantProfile}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvMerchantProfile extends GenericItem
{
	/** Qualifier of the <code>IsvMerchantProfile.id</code> attribute **/
	public static final String ID = "id";
	/** Qualifier of the <code>IsvMerchantProfile.profileType</code> attribute **/
	public static final String PROFILETYPE = "profileType";
	/** Qualifier of the <code>IsvMerchantProfile.profileId</code> attribute **/
	public static final String PROFILEID = "profileId";
	/** Qualifier of the <code>IsvMerchantProfile.accessKey</code> attribute **/
	public static final String ACCESSKEY = "accessKey";
	/** Qualifier of the <code>IsvMerchantProfile.secretKey</code> attribute **/
	public static final String SECRETKEY = "secretKey";
	/** Qualifier of the <code>IsvMerchantProfile.merchant</code> attribute **/
	public static final String MERCHANT = "merchant";
	/**
	* {@link BidirectionalOneToManyHandler} for handling 1:n MERCHANT's relation attributes from 'one' side.
	**/
	protected static final BidirectionalOneToManyHandler<GeneratedIsvMerchantProfile> MERCHANTHANDLER = new BidirectionalOneToManyHandler<GeneratedIsvMerchantProfile>(
	IsvPaymentConstants.TC.ISVMERCHANTPROFILE,
	false,
	"merchant",
	null,
	false,
	true,
	CollectionType.COLLECTION
	);
	protected static final Map<String, AttributeMode> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>();
		tmp.put(ID, AttributeMode.INITIAL);
		tmp.put(PROFILETYPE, AttributeMode.INITIAL);
		tmp.put(PROFILEID, AttributeMode.INITIAL);
		tmp.put(ACCESSKEY, AttributeMode.INITIAL);
		tmp.put(SECRETKEY, AttributeMode.INITIAL);
		tmp.put(MERCHANT, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.accessKey</code> attribute.
	 * @return the accessKey
	 */
	public String getAccessKey(final SessionContext ctx)
	{
		return (String)getProperty( ctx, ACCESSKEY);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.accessKey</code> attribute.
	 * @return the accessKey
	 */
	public String getAccessKey()
	{
		return getAccessKey( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.accessKey</code> attribute. 
	 * @param value the accessKey
	 */
	public void setAccessKey(final SessionContext ctx, final String value)
	{
		setProperty(ctx, ACCESSKEY,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.accessKey</code> attribute. 
	 * @param value the accessKey
	 */
	public void setAccessKey(final String value)
	{
		setAccessKey( getSession().getSessionContext(), value );
	}
	
	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes) throws JaloBusinessException
	{
		MERCHANTHANDLER.newInstance(ctx, allAttributes);
		return super.createItem( ctx, type, allAttributes );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.id</code> attribute.
	 * @return the id
	 */
	public String getId(final SessionContext ctx)
	{
		return (String)getProperty( ctx, ID);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.id</code> attribute.
	 * @return the id
	 */
	public String getId()
	{
		return getId( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.id</code> attribute. 
	 * @param value the id
	 */
	public void setId(final SessionContext ctx, final String value)
	{
		setProperty(ctx, ID,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.id</code> attribute. 
	 * @param value the id
	 */
	public void setId(final String value)
	{
		setId( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant(final SessionContext ctx)
	{
		return (IsvMerchant)getProperty( ctx, MERCHANT);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.merchant</code> attribute.
	 * @return the merchant
	 */
	public IsvMerchant getMerchant()
	{
		return getMerchant( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final SessionContext ctx, final IsvMerchant value)
	{
		MERCHANTHANDLER.addValue( ctx, value, this  );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.merchant</code> attribute. 
	 * @param value the merchant
	 */
	public void setMerchant(final IsvMerchant value)
	{
		setMerchant( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.profileId</code> attribute.
	 * @return the profileId
	 */
	public String getProfileId(final SessionContext ctx)
	{
		return (String)getProperty( ctx, PROFILEID);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.profileId</code> attribute.
	 * @return the profileId
	 */
	public String getProfileId()
	{
		return getProfileId( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.profileId</code> attribute. 
	 * @param value the profileId
	 */
	public void setProfileId(final SessionContext ctx, final String value)
	{
		setProperty(ctx, PROFILEID,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.profileId</code> attribute. 
	 * @param value the profileId
	 */
	public void setProfileId(final String value)
	{
		setProfileId( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.profileType</code> attribute.
	 * @return the profileType
	 */
	public EnumerationValue getProfileType(final SessionContext ctx)
	{
		return (EnumerationValue)getProperty( ctx, PROFILETYPE);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.profileType</code> attribute.
	 * @return the profileType
	 */
	public EnumerationValue getProfileType()
	{
		return getProfileType( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.profileType</code> attribute. 
	 * @param value the profileType
	 */
	public void setProfileType(final SessionContext ctx, final EnumerationValue value)
	{
		setProperty(ctx, PROFILETYPE,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.profileType</code> attribute. 
	 * @param value the profileType
	 */
	public void setProfileType(final EnumerationValue value)
	{
		setProfileType( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.secretKey</code> attribute.
	 * @return the secretKey
	 */
	public String getSecretKey(final SessionContext ctx)
	{
		return (String)getProperty( ctx, SECRETKEY);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchantProfile.secretKey</code> attribute.
	 * @return the secretKey
	 */
	public String getSecretKey()
	{
		return getSecretKey( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.secretKey</code> attribute. 
	 * @param value the secretKey
	 */
	public void setSecretKey(final SessionContext ctx, final String value)
	{
		setProperty(ctx, SECRETKEY,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchantProfile.secretKey</code> attribute. 
	 * @param value the secretKey
	 */
	public void setSecretKey(final String value)
	{
		setSecretKey( getSession().getSessionContext(), value );
	}
	
}
