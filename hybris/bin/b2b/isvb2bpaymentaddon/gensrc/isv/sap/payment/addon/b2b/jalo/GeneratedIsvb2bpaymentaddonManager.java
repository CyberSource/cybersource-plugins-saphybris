/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Dec 13, 2022, 12:07:34 PM                   ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.addon.b2b.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.extension.Extension;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import isv.sap.payment.addon.b2b.constants.Isvb2bpaymentaddonConstants;
import isv.sap.payment.addon.b2b.jalo.ReplenishmentInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type <code>Isvb2bpaymentaddonManager</code>.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvb2bpaymentaddonManager extends Extension
{
	protected static final Map<String, Map<String, AttributeMode>> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, Map<String, AttributeMode>> ttmp = new HashMap();
		Map<String, AttributeMode> tmp = new HashMap<String, AttributeMode>();
		tmp.put("replenishmentInfo", AttributeMode.INITIAL);
		ttmp.put("de.hybris.platform.jalo.order.Cart", Collections.unmodifiableMap(tmp));
		DEFAULT_INITIAL_ATTRIBUTES = ttmp;
	}
	@Override
	public Map<String, AttributeMode> getDefaultAttributeModes(final Class<? extends Item> itemClass)
	{
		Map<String, AttributeMode> ret = new HashMap<>();
		final Map<String, AttributeMode> attr = DEFAULT_INITIAL_ATTRIBUTES.get(itemClass.getName());
		if (attr != null)
		{
			ret.putAll(attr);
		}
		return ret;
	}
	
	public ReplenishmentInfo createReplenishmentInfo(final SessionContext ctx, final Map attributeValues)
	{
		try
		{
			ComposedType type = getTenant().getJaloConnection().getTypeManager().getComposedType( Isvb2bpaymentaddonConstants.TC.REPLENISHMENTINFO );
			return (ReplenishmentInfo)type.newInstance( ctx, attributeValues );
		}
		catch( JaloGenericCreationException e)
		{
			final Throwable cause = e.getCause();
			throw (cause instanceof RuntimeException ?
			(RuntimeException)cause
			:
			new JaloSystemException( cause, cause.getMessage(), e.getErrorCode() ) );
		}
		catch( JaloBusinessException e )
		{
			throw new JaloSystemException( e ,"error creating ReplenishmentInfo : "+e.getMessage(), 0 );
		}
	}
	
	public ReplenishmentInfo createReplenishmentInfo(final Map attributeValues)
	{
		return createReplenishmentInfo( getSession().getSessionContext(), attributeValues );
	}
	
	@Override
	public String getName()
	{
		return Isvb2bpaymentaddonConstants.EXTENSIONNAME;
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>Cart.replenishmentInfo</code> attribute.
	 * @return the replenishmentInfo
	 */
	public ReplenishmentInfo getReplenishmentInfo(final SessionContext ctx, final Cart item)
	{
		return (ReplenishmentInfo)item.getProperty( ctx, Isvb2bpaymentaddonConstants.Attributes.Cart.REPLENISHMENTINFO);
	}
	
	/**
	 * <i>Generated method</i> - Getter of the <code>Cart.replenishmentInfo</code> attribute.
	 * @return the replenishmentInfo
	 */
	public ReplenishmentInfo getReplenishmentInfo(final Cart item)
	{
		return getReplenishmentInfo( getSession().getSessionContext(), item );
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>Cart.replenishmentInfo</code> attribute. 
	 * @param value the replenishmentInfo
	 */
	public void setReplenishmentInfo(final SessionContext ctx, final Cart item, final ReplenishmentInfo value)
	{
		item.setProperty(ctx, Isvb2bpaymentaddonConstants.Attributes.Cart.REPLENISHMENTINFO,value);
	}
	
	/**
	 * <i>Generated method</i> - Setter of the <code>Cart.replenishmentInfo</code> attribute. 
	 * @param value the replenishmentInfo
	 */
	public void setReplenishmentInfo(final Cart item, final ReplenishmentInfo value)
	{
		setReplenishmentInfo( getSession().getSessionContext(), item, value );
	}
	
}
