package isv.sap.payment.addon.facade.impl;

import java.util.Arrays;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorfacades.payment.impl.DefaultPaymentFacade;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.MerchantService;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

public abstract class AbstractPaymentFacade extends DefaultPaymentFacade
{
    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    protected PaymentServiceResult executeRequest(final PaymentServiceRequest paymentServiceRequest)
    {
        return paymentServiceExecutor.execute(paymentServiceRequest);
    }

    protected static boolean isTransactionInState(final IsvPaymentTransactionEntryModel transaction,
            final String... statuses)
    {
        return Arrays.stream(statuses).anyMatch(status -> status.equalsIgnoreCase(transaction.getTransactionStatus()));
    }

    protected String getMerchantID(final PaymentType paymentType)
    {
        return merchantService.getCurrentMerchant(paymentType).getId();
    }

    protected String convertToAbsoluteURL(final String relativeURL, final boolean secure)
    {
        return getSiteBaseUrlResolutionService()
                .getWebsiteUrlForSite(getBaseSiteService().getCurrentBaseSite(), secure, relativeURL);
    }

    public PaymentServiceExecutor getPaymentServiceExecutor()
    {
        return paymentServiceExecutor;
    }

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }

    public MerchantService getMerchantService()
    {
        return merchantService;
    }

    public void setMerchantService(final MerchantService merchantService)
    {
        this.merchantService = merchantService;
    }
}
