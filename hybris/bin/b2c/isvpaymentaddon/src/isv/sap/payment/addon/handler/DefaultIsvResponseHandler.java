package isv.sap.payment.addon.handler;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import isv.cjl.payment.enums.PaymentSource;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.model.Merchant;
import isv.cjl.payment.model.MerchantProfile;
import isv.cjl.payment.security.service.SAService;
import isv.cjl.payment.service.MerchantService;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.addon.utils.HttpRequestUtil;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.utils.LogUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;

public class DefaultIsvResponseHandler implements ResponseHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultIsvResponseHandler.class);

    @Resource(name = "isv.cjl.payment.security.service.SAService")
    private SAService saService;

    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    private Map<String, PaymentTransactionType> transactionTypeMap;

    private static boolean isAuthorizationDuplicateTransaction(final String reasonCode,
            final String decision)
    {
        return IsvPaymentConstants.ReasonCode.DUPLICATE_TRANSACTION.equals(reasonCode)
                && IsvPaymentConstants.TransactionStatus.ERROR.equals(decision);
    }

    @Override
    public Map<String, String> getValidParameters(final HttpServletRequest request)
    {
        final Map<String, String> paymentData = HttpRequestUtil.getParametersMap(request);

        return saService.getValidParameters(paymentData);
    }

    @Override
    public void processResponse(final AbstractOrderModel order, final Map<String, String> paymentResponse)
    {
        final String merchantId = paymentResponse.get(IsvPaymentConstants.SAResponseFields.MERCHANT_ID);

        final String transactionType = paymentResponse.get("req_transaction_type");

        final PaymentServiceRequest request = PaymentServiceRequest.create()
                .source(PaymentSource.SECURE_ACCEPTANCE)
                .method(PaymentType.CREDIT_CARD)
                .service(transactionTypeMap.get(transactionType))
                .addParam("merchantId", merchantId)
                .addParam(ORDER, order)
                .addParam("paymentResponse", paymentResponse);

        paymentServiceExecutor.execute(request);
    }

    public boolean isDecisionSuccessful(final Map<String, String> paymentResponse)
    {
        final String decision = paymentResponse.get(IsvPaymentConstants.SAResponseFields.DECISION);
        final String orderNumber = paymentResponse.get(IsvPaymentConstants.SAResponseFields.REFERENCE_NUMBER);
        final String reasonCode = paymentResponse.get(IsvPaymentConstants.SAResponseFields.REASON_CODE);

        LOG.info("Secure Acceptance receipt page, decision is: {} for order : {}", LogUtils.encode(decision), LogUtils.encode(orderNumber));

        return IsvPaymentConstants.TransactionStatus.ACCEPT.equals(decision)
                || IsvPaymentConstants.TransactionStatus.REVIEW.equals(decision)
                || isAuthorizationDuplicateTransaction(reasonCode, decision);
    }

    public boolean isValidSignature(final Map<String, String> paymentResponse)
    {
        final String merchantId = paymentResponse.get(IsvPaymentConstants.SAResponseFields.MERCHANT_ID);
        final String profileType = paymentResponse.get(IsvPaymentConstants.SAResponseFields.PROFILE_TYPE);

        final Merchant merchant = merchantService.getMerchant(merchantId);
        final MerchantProfile merchantProfile = merchantService.getMerchantProfile(merchant, profileType);

        if (!saService.validateTransactionSignature(paymentResponse, merchantProfile.getSecretKey()))
        {
            LOG.warn("The signature for the isv payment response is not valid");
            return false;
        }

        return true;
    }

    @Required
    public void setTransactionTypeMap(final Map<String, PaymentTransactionType> transactionTypeMap)
    {
        this.transactionTypeMap = transactionTypeMap;
    }

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }
}
