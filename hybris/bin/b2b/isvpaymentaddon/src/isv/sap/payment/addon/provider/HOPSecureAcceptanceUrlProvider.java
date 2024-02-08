package isv.sap.payment.addon.provider;

import javax.annotation.Resource;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.site.BaseSiteService;

public class HOPSecureAcceptanceUrlProvider implements SecureAcceptanceUrlProvider
{
    public static final String SUBSCRIPTION_URL_KEY = "isv.payment.secure.acceptance.hop.subscription.create.post.url";

    public static final String AUTHORIZATION_URL_KEY = "isv.payment.secure.acceptance.hop.post.url";

    public static final String CREATE_PAYMENT_TOKEN = "create_payment_token";

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private BaseSiteService baseSiteService;

    @Override
    public String getURL()
    {
        final String key = isSubscriptionBased() ? SUBSCRIPTION_URL_KEY : AUTHORIZATION_URL_KEY;
        return configurationService.getConfiguration().getString(key);
    }

    private boolean isSubscriptionBased()
    {
        final String key = "secure.acceptance." + baseSiteService.getCurrentBaseSite().getUid() + ".transaction.type";
        final String value = configurationService.getConfiguration().getString(key);

        return CREATE_PAYMENT_TOKEN.equals(value);
    }
}
