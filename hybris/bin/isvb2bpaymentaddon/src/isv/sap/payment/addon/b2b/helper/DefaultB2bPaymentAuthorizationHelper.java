package isv.sap.payment.addon.b2b.helper;

import javax.annotation.Resource;

import com.google.common.collect.ImmutableMap;
import de.hybris.platform.core.model.order.CartModel;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.creditcard.AuthorizationRequestBuilder;
import isv.sap.payment.configuration.service.PaymentConfigurationService;
import isv.sap.payment.enums.IsvConfigurationType;
import isv.sap.payment.enums.IsvPaymentChannel;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.CURRENCY;
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.PAYMENTCHANNEL;
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.PAYMENTTYPE;
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.SITE;

public class DefaultB2bPaymentAuthorizationHelper implements B2bPaymentAuthorizationHelper
{
    private static final String MERCHANT_ID_KEY = "req_merchant_defined_data99";

    private static final String SUBSCRIPTION_ID_KEY = "payment_token";

    @Resource(name = "isv.sap.payment.paymentConfigurationService")
    private PaymentConfigurationService paymentConfigurationService;

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Override
    public IsvPaymentTransactionEntryModel authorizePayment(final IsvPaymentTransactionEntryModel subscriptionEntry,
            final CartModel cart)
    {
        return authorizePayment(subscriptionEntry, cart, null);
    }

    @Override
    public IsvPaymentTransactionEntryModel authorizeRecurringPayment(
            final IsvPaymentTransactionEntryModel subscriptionEntry, final CartModel cart)
    {
        return authorizePayment(subscriptionEntry, cart, getAuthServiceCommerceIndicator(cart));
    }

    private IsvPaymentTransactionEntryModel authorizePayment(final IsvPaymentTransactionEntryModel subscriptionEntry,
            final CartModel cart,
            final String authServiceCommerceIndicator)
    {
        final String subscriptionId = subscriptionEntry.getProperties().get(SUBSCRIPTION_ID_KEY);
        final String merchantId = subscriptionEntry.getProperties().get(MERCHANT_ID_KEY);

        final PaymentServiceRequest request = new AuthorizationRequestBuilder()
                .addParam("order", cart)
                .setMerchantId(merchantId)
                .setSubscriptionID(subscriptionId)
                .setAuthServiceCommerceIndicator(authServiceCommerceIndicator)
                .build();

        return paymentServiceExecutor.execute(request).getData(TRANSACTION);
    }

    private String getAuthServiceCommerceIndicator(final CartModel cart)
    {
        final IsvMerchantPaymentConfigurationModel paymentConfiguration = paymentConfigurationService
                .getConfiguration(
                        IsvConfigurationType.MERCHANT_CONFIG,
                        ImmutableMap.of(
                                SITE, cart.getSite(),
                                PAYMENTTYPE, PaymentType.CREDIT_CARD,
                                PAYMENTCHANNEL, IsvPaymentChannel.WEB,
                                CURRENCY, cart.getCurrency()
                        )
                );

        final String authServiceCommerceIndicator = paymentConfiguration.getAuthServiceCommerceIndicator();

        if (StringUtils.isBlank(authServiceCommerceIndicator))
        {
            throw new IllegalStateException(
                    "The commerce indicator is missing, please check your merchant configuration");
        }

        return authServiceCommerceIndicator;
    }

    public void setPaymentConfigurationService(final PaymentConfigurationService paymentConfigurationService)
    {
        this.paymentConfigurationService = paymentConfigurationService;
    }
}
