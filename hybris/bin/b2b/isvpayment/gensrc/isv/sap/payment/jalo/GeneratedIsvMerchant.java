/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.jalo;

import de.hybris.platform.jalo.GenericItem;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.CollectionType;
import de.hybris.platform.util.OneToManyHandler;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.jalo.IsvMerchantProfile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type {@link isv.sap.payment.jalo.IsvMerchant IsvMerchant}.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvMerchant extends GenericItem
{
	/** Qualifier of the <code>IsvMerchant.id</code> attribute **/
	public static final String ID = "id";
	/** Qualifier of the <code>IsvMerchant.userName</code> attribute **/
	public static final String USERNAME = "userName";
	/** Qualifier of the <code>IsvMerchant.passwordToken</code> attribute **/
	public static final String PASSWORDTOKEN = "passwordToken";
	/** Qualifier of the <code>IsvMerchant.profiles</code> attribute **/
	public static final String PROFILES = "profiles";
	/**
	* {@link OneToManyHandler} for handling 1:n PROFILES's relation attributes from 'many' side.
	**/
	protected static final OneToManyHandler<IsvMerchantProfile> PROFILESHANDLER = new OneToManyHandler<IsvMerchantProfile>(
	IsvPaymentConstants.TC.ISVMERCHANTPROFILE,
	true,
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
		tmp.put(USERNAME, AttributeMode.INITIAL);
		tmp.put(PASSWORDTOKEN, AttributeMode.INITIAL);
		DEFAULT_INITIAL_ATTRIBUTES = Collections.unmodifiableMap(tmp);
	}
	@Override
	protected Map<String, AttributeMode> getDefaultAttributeModes()
	{
		return DEFAULT_INITIAL_ATTRIBUTES;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.id</code> attribute.
	 * @return the id
	 */
	public String getId(final SessionContext ctx)
	{
		return (String)getProperty( ctx, ID);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.id</code> attribute.
	 * @return the id
	 */
	public String getId()
	{
		return getId( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.id</code> attribute. 
	 * @param value the id
	 */
	public void setId(final SessionContext ctx, final String value)
	{
		setProperty(ctx, ID,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.id</code> attribute. 
	 * @param value the id
	 */
	public void setId(final String value)
	{
		setId( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.passwordToken</code> attribute.
	 * @return the passwordToken - SO API credentials: password token
	 */
	public String getPasswordToken(final SessionContext ctx)
	{
		return (String)getProperty( ctx, PASSWORDTOKEN);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.passwordToken</code> attribute.
	 * @return the passwordToken - SO API credentials: password token
	 */
	public String getPasswordToken()
	{
		return getPasswordToken( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.passwordToken</code> attribute. 
	 * @param value the passwordToken - SO API credentials: password token
	 */
	public void setPasswordToken(final SessionContext ctx, final String value)
	{
		setProperty(ctx, PASSWORDTOKEN,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.passwordToken</code> attribute. 
	 * @param value the passwordToken - SO API credentials: password token
	 */
	public void setPasswordToken(final String value)
	{
		setPasswordToken( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.profiles</code> attribute.
	 * @return the profiles
	 */
	public Collection<IsvMerchantProfile> getProfiles(final SessionContext ctx)
	{
		return PROFILESHANDLER.getValues( ctx, this );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.profiles</code> attribute.
	 * @return the profiles
	 */
	public Collection<IsvMerchantProfile> getProfiles()
	{
		return getProfiles( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.profiles</code> attribute. 
	 * @param value the profiles
	 */
	public void setProfiles(final SessionContext ctx, final Collection<IsvMerchantProfile> value)
	{
		PROFILESHANDLER.setValues( ctx, this, value );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.profiles</code> attribute. 
	 * @param value the profiles
	 */
	public void setProfiles(final Collection<IsvMerchantProfile> value)
	{
		setProfiles( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Adds <code>value</code> to profiles. 
	 * @param value the item to add to profiles
	 */
	public void addToProfiles(final SessionContext ctx, final IsvMerchantProfile value)
	{
		PROFILESHANDLER.addValue( ctx, this, value );
	}
	
	/**
	 * <i>Generated method</i> - Adds <code>value</code> to profiles. 
	 * @param value the item to add to profiles
	 */
	public void addToProfiles(final IsvMerchantProfile value)
	{
		addToProfiles( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Removes <code>value</code> from profiles. 
	 * @param value the item to remove from profiles
	 */
	public void removeFromProfiles(final SessionContext ctx, final IsvMerchantProfile value)
	{
		PROFILESHANDLER.removeValue( ctx, this, value );
	}
	
	/**
	 * <i>Generated method</i> - Removes <code>value</code> from profiles. 
	 * @param value the item to remove from profiles
	 */
	public void removeFromProfiles(final IsvMerchantProfile value)
	{
		removeFromProfiles( getSession().getSessionContext(), value );
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.userName</code> attribute.
	 * @return the userName - SO API credentials: user name
	 */
	public String getUserName(final SessionContext ctx)
	{
		return (String)getProperty( ctx, USERNAME);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>IsvMerchant.userName</code> attribute.
	 * @return the userName - SO API credentials: user name
	 */
	public String getUserName()
	{
		return getUserName( getSession().getSessionContext() );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.userName</code> attribute. 
	 * @param value the userName - SO API credentials: user name
	 */
	public void setUserName(final SessionContext ctx, final String value)
	{
		setProperty(ctx, USERNAME,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>IsvMerchant.userName</code> attribute. 
	 * @param value the userName - SO API credentials: user name
	 */
	public void setUserName(final String value)
	{
		setUserName( getSession().getSessionContext(), value );
	}
	
}
