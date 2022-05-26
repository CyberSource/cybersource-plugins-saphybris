/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Mar 17, 2021, 4:34:29 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.addon.b2c.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.Item.AttributeMode;
import de.hybris.platform.jalo.extension.Extension;
import isv.sap.payment.addon.b2c.constants.Isvb2cpaymentaddonConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated class for type <code>Isvb2cpaymentaddonManager</code>.
 */
@SuppressWarnings({"deprecation","unused","cast"})
public abstract class GeneratedIsvb2cpaymentaddonManager extends Extension
{
	protected static final Map<String, Map<String, AttributeMode>> DEFAULT_INITIAL_ATTRIBUTES;
	static
	{
		final Map<String, Map<String, AttributeMode>> ttmp = new HashMap();
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
	
	@Override
	public String getName()
	{
		return Isvb2cpaymentaddonConstants.EXTENSIONNAME;
	}
	
}
