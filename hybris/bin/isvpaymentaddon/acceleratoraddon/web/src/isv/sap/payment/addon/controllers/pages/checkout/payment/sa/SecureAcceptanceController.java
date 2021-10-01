package isv.sap.payment.addon.controllers.pages.checkout.payment.sa;

import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.site.BaseSiteService;

import isv.cjl.payment.service.MerchantService;
import isv.sap.payment.data.PaymentSystemInfo;

public class SecureAcceptanceController extends AbstractPageController
{
    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    @Resource
    private BaseSiteService siteService;

    @Resource
    private I18NService i18NService;

    @Resource
    private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentSystemInfo")
    private PaymentSystemInfo paymentSystemInfo;

    protected String getTransactionType()
    {
        final String key = "secure.acceptance." + siteService.getCurrentBaseSite().getUid() + ".transaction.type";
        return configurationService.getConfiguration().getString(key);
    }

    protected String getReceiptUrl()
    {
        return siteBaseUrlResolutionService.getWebsiteUrlForSite(siteService.getCurrentBaseSite(), true,
                "/checkout/payment/sa/receipt");
    }

    protected String getMerchantPostUrl()
    {
        return siteBaseUrlResolutionService.getWebsiteUrlForSite(siteService.getCurrentBaseSite(), true,
                "/checkout/payment/sa/merchantpost");
    }

    protected I18NService getI18NService()
    {
        return i18NService;
    }

    protected void setI18NService(final I18NService i18NService)
    {
        this.i18NService = i18NService;
    }

    protected ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    protected void setConfigurationService(final ConfigurationService configurationService)
    {
        this.configurationService = configurationService;
    }

    protected CartService getCartService()
    {
        return cartService;
    }

    protected void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }

    protected MerchantService getMerchantService()
    {
        return merchantService;
    }

    protected void setMerchantService(final MerchantService merchantService)
    {
        this.merchantService = merchantService;
    }

    protected PaymentSystemInfo getPaymentSystemInfo()
    {
        return paymentSystemInfo;
    }

    protected void setPaymentSystemInfo(final PaymentSystemInfo paymentSystemInfo)
    {
        this.paymentSystemInfo = paymentSystemInfo;
    }

    public SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
    {
        return siteBaseUrlResolutionService;
    }

    protected void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
    {
        this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
    }

    public BaseSiteService getSiteService()
    {
        return siteService;
    }

    protected void setSiteService(final BaseSiteService siteService)
    {
        this.siteService = siteService;
    }
}
