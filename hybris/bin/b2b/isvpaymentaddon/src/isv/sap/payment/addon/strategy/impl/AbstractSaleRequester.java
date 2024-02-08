package isv.sap.payment.addon.strategy.impl;

import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.site.BaseSiteService;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.sap.payment.addon.strategy.AlternativePaymentSaleRequester;

public abstract class AbstractSaleRequester implements AlternativePaymentSaleRequester
{
    protected static final String MERCHANT_NAME = "isv.payment.alternativepayment.merchantName";

    @Resource
    private SiteConfigService siteConfigService;

    @Resource
    private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

    @Resource
    private BaseSiteService baseSiteService;

    @Resource
    private ConfigurationService configurationService;

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Override
    public PaymentServiceResult initiateSale(final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType,
            final String merchantId, final Map<String, Object> optionalParams)
    {
        if (!supports(paymentType))
        {
            throw new IllegalArgumentException("Given requester doesn't supports: " + paymentType);
        }
        return internalInitiateSale(cart, paymentType, merchantId, optionalParams);
    }

    protected abstract PaymentServiceResult internalInitiateSale(final AbstractOrderModel cart,
            final AlternativePaymentMethod paymentType, final String merchantId,
            final Map<String, Object> optionalParams);

    @Override
    public boolean supports(final AlternativePaymentMethod paymentType)
    {
        ServicesUtil.validateParameterNotNullStandardMessage("paymentType", paymentType);

        return internalSupports(paymentType);
    }

    protected abstract boolean internalSupports(final AlternativePaymentMethod paymentType);

    protected String convertToAbsoluteURL(final String relativeURL, final boolean secure)
    {
        return getSiteBaseUrlResolutionService().
                getWebsiteUrlForSite(getBaseSiteService().getCurrentBaseSite(), secure, relativeURL);
    }

    public SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
    {
        return siteBaseUrlResolutionService;
    }

    public void setSiteBaseUrlResolutionService(
            final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
    {
        this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
    }

    public BaseSiteService getBaseSiteService()
    {
        return baseSiteService;
    }

    public void setBaseSiteService(final BaseSiteService baseSiteService)
    {
        this.baseSiteService = baseSiteService;
    }

    public PaymentServiceExecutor getPaymentServiceExecutor()
    {
        return paymentServiceExecutor;
    }

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService(final ConfigurationService configurationService)
    {
        this.configurationService = configurationService;
    }

    protected String getProperty(final String key)
    {
        return configurationService.getConfiguration().getString(key);
    }

    protected int getIntProperty(final String key, final int defaultValue)
    {
        return configurationService.getConfiguration().getInt(key, defaultValue);
    }

    protected SiteConfigService getSiteConfigService()
    {
        return siteConfigService;
    }

    public void setSiteConfigService(final SiteConfigService siteConfigService)
    {
        this.siteConfigService = siteConfigService;
    }
}
