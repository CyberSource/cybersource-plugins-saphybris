package isv.sap.payment.addon.facade.impl;

import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.CartModel;

import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.enums.DecryptionType;
import isv.cjl.payment.service.applepay.ApplePayDecryptionService;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.applepay.AuthorizationRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.applepay.CreateSessionRequestBuilder;
import isv.cjl.payment.utils.PaymentHelper;
import isv.cjl.payment.utils.PaymentParamUtils;
import isv.sap.payment.addon.facade.ApplePayPaymentFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.ApplePay.CreateSession.SESSION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_DECRYPTION_TYPE;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_INITIATIVE;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_INITIATIVE_CONTEXT;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_KEYSTORE_LOCATION;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_KEYSTORE_PASSWORD;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationKeys.APPLE_PAY_MERCHANT_IDENTIFIER;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REVIEW;
import static isv.cjl.payment.enums.PaymentType.APPLE_PAY;

public class ApplePayPaymentFacadeImpl extends AbstractPaymentFacade implements ApplePayPaymentFacade
{
    @Resource(name = "isv.sap.payment.applePayDecryptionService")
    private ApplePayDecryptionService applePayDecryptionService;

    @Resource(name = "isv.cjl.payment.utils.paymentHelper")
    private PaymentHelper paymentHelper;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> createApplePaySession(final String validationUrl)
    {
        final String merchantId = getSiteConfigService().getProperty(APPLE_PAY_MERCHANT_IDENTIFIER);
        final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();
        final String displayName = currentBaseSite.getName();
        final String initiative = getSiteConfigService().getProperty(APPLE_PAY_INITIATIVE);
        final String initiativeContext = getSiteConfigService()
                .getProperty(String.format(APPLE_PAY_INITIATIVE_CONTEXT, currentBaseSite.getUid()));
        final String keystoreLocation = getSiteConfigService().getProperty(APPLE_PAY_KEYSTORE_LOCATION);
        final String keystorePassword = getSiteConfigService().getProperty(APPLE_PAY_KEYSTORE_PASSWORD);

        final PaymentServiceResult result = executeRequest(new CreateSessionRequestBuilder()
                .setMerchantId(merchantId)
                .setValidationURL(validationUrl)
                .setDisplayName(displayName)
                .addParam("initiative", initiative)
                .setInitiativeContext(initiativeContext)
                .addParam("keystoreLocation", keystoreLocation)
                .addParam("keystorePassword", keystorePassword)
                .build());

        return (Map) result.getData().get(SESSION);
    }

    @Override
    public boolean authorizeApplePayPayment(final Map<String, Object> paymentToken, final CartModel cart)
    {
        final DecryptionType decryptionType = DecryptionType
                .valueOf(getSiteConfigService().getProperty(APPLE_PAY_DECRYPTION_TYPE));
        final Map<String, Object> data = DecryptionType.MERCHANT.equals(decryptionType)
                ? decryptApplePaymentData(paymentToken)
                : paymentToken;

        final Map<String, Object> token = PaymentParamUtils.getParam("token", paymentToken);
        final Map<String, Object> paymentMethod = PaymentParamUtils.getParam("paymentMethod", token);
        final String network = PaymentParamUtils.getParam("network", paymentMethod);
        final CardType cardType = paymentHelper.getCardType(network);

        final PaymentServiceResult authorizationResult = executeRequest(new AuthorizationRequestBuilder()
                        .setMerchantId(getMerchantService().getCurrentMerchant(APPLE_PAY).getId())
                        .setPaymentToken(data)
                        .addParam(ORDER, cart)
                        .setDecryptionType(decryptionType)
                        .setCardType(cardType)
                        .build());

        final IsvPaymentTransactionEntryModel transaction = authorizationResult.getData(TRANSACTION);

        return isTransactionInState(transaction, ACCEPT, REVIEW);
    }

    @Override
    public Map<String, Object> decryptApplePaymentData(final Map<String, Object> paymentToken)
    {
        return applePayDecryptionService.decrypt(paymentToken);
    }

    public void setApplePayDecryptionService(final ApplePayDecryptionService applePayDecryptionService)
    {
        this.applePayDecryptionService = applePayDecryptionService;
    }

    public void setPaymentHelper(final PaymentHelper paymentHelper)
    {
        this.paymentHelper = paymentHelper;
    }
}
