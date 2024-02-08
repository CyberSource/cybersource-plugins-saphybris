package isv.sap.payment.addon.facade.impl;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.PaymentServiceRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.paypal.AuthorizationRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.paypal.CheckStatusRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.paypal.CreateSessionRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.paypal.OrderSetupRequestBuilder;
import isv.sap.payment.addon.facade.PayPalPaymentFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.PAYER_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REVIEW;
import static isv.sap.payment.enums.PaymentType.PAY_PAL;
import static java.lang.String.format;

public class PayPalPaymentFacadeImpl extends AbstractPaymentFacade implements PayPalPaymentFacade
{
    private static final String AP_SESSIONS_REPLY_MERCHANT_URL = "apSessionsReplyMerchantURL";

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Value("${isv.payment.paypal.return.url:}")
    private String paypalRelativeReturnUrl;

    @Value("${isv.payment.paypal.cancelReturn.url:}")
    private String paypalRelativeCancelUrl;

    private static Predicate<PaymentTransactionModel> doesCartHavePayPalTransaction()
    {
        return txn -> PAY_PAL.name().equals(txn.getPaymentProvider()) && txn instanceof IsvPaymentTransactionModel;
    }

    @Override
    public boolean authorizePayPalPayment(final CartModel cart, final String ecToken)
    {
        final Optional<IsvPaymentTransactionModel> payPalTransactionOptional = findCorrespondingPayPalPaymentTransaction(
                ecToken, cart);
        if (payPalTransactionOptional.isEmpty())
        {
            throw new IllegalStateException(
                    "Cart: " + cart.getGuid() + " doesn't have valid paypal CREATE SESSION transaction entry");
        }

        final IsvPaymentTransactionModel createSessionTransaction = payPalTransactionOptional.get();
        final String merchantId = createSessionTransaction.getMerchantId();

        final IsvPaymentTransactionEntryModel checkStatusTransactionEntry = doPayPalCheckStatus(cart,
                createSessionTransaction, merchantId);

        if (isTransactionInState(checkStatusTransactionEntry, ACCEPT))
        {
            final IsvPaymentTransactionEntryModel orderSetupTransactionEntry = doPayPalOrderSetup(cart,
                    createSessionTransaction,
                    checkStatusTransactionEntry, merchantId);

            if (isTransactionInState(orderSetupTransactionEntry, ACCEPT))
            {
                final IsvPaymentTransactionEntryModel authTransactionEntry = doPayPalAuthorize(cart, merchantId,
                        orderSetupTransactionEntry);

                return isTransactionInState(authTransactionEntry, ACCEPT, REVIEW);
            }
        }

        return false;
    }

    @Override
    public String executePayPalExpressCheckoutCreateSessionRequest(final CartModel cart)
    {
        final PaymentServiceRequest paymentServiceRequest = new CreateSessionRequestBuilder()
                .addParam(ORDER, cart)
                .setMerchantId(getMerchantID(isv.cjl.payment.enums.PaymentType.PAY_PAL))
                .setSuccessURL(convertToAbsoluteURL(paypalRelativeReturnUrl, true))
                .setCancelURL(convertToAbsoluteURL(paypalRelativeCancelUrl, true))
                .build();

        final PaymentServiceResult paymentServiceResult = executeRequest(paymentServiceRequest);
        final IsvPaymentTransactionEntryModel transactionEntry = paymentServiceResult.getData(TRANSACTION);

        if (isTransactionInState(transactionEntry, ACCEPT))
        {
            return transactionEntry.getProperties().get(AP_SESSIONS_REPLY_MERCHANT_URL);
        }
        else
        {
            throw new PaymentException(
                    format("paypal Set action rejected with status [%s]", transactionEntry.getTransactionStatus()));
        }
    }

    private IsvPaymentTransactionEntryModel doPayPalAuthorize(final CartModel cart, final String merchantId,
            final IsvPaymentTransactionEntryModel orderSetupTransactionEntry)
    {
        return executePaypalCommand(new AuthorizationRequestBuilder()
                        .addParam(ORDER, cart)
                        .addParam(TRANSACTION, orderSetupTransactionEntry)
                        .setMerchantId(merchantId));
    }

    private IsvPaymentTransactionEntryModel doPayPalOrderSetup(final CartModel cart,
            final IsvPaymentTransactionModel createSessionTransaction,
            final IsvPaymentTransactionEntryModel checkStatusTransactionEntryModel, final String merchantId)
    {
        final Optional<PaymentTransactionEntryModel> createSessionTransactionEntryModel = paymentTransactionService
                .getLatestAcceptedTransactionEntry(
                        createSessionTransaction, PaymentTransactionType.CREATE_SESSION);

        return executePaypalCommand(new OrderSetupRequestBuilder()
                .addParam(ORDER, cart)
                .addParam(TRANSACTION, createSessionTransactionEntryModel.get())
                .addParam(PAYER_ID, checkStatusTransactionEntryModel.getProperties().get("apReplyPayerID"))
                .setMerchantId(merchantId));
    }

    private IsvPaymentTransactionEntryModel doPayPalCheckStatus(final CartModel cart,
            final IsvPaymentTransactionModel createSessionTransaction,
            final String merchantId)
    {
        final Optional<PaymentTransactionEntryModel> createSessionTransactionEntry = paymentTransactionService
                .getLatestAcceptedTransactionEntry(
                        createSessionTransaction, PaymentTransactionType.CREATE_SESSION);

        return executePaypalCommand(new CheckStatusRequestBuilder()
                .addParam(ORDER, cart)
                .addParam(TRANSACTION, createSessionTransactionEntry.get())
                .setMerchantId(merchantId));
    }

    private IsvPaymentTransactionEntryModel executePaypalCommand(final PaymentServiceRequestBuilder requestBuilder)
    {
        final PaymentServiceResult orderSetupResult = executeRequest(requestBuilder.build());

        return orderSetupResult.getData(TRANSACTION);
    }

    private Optional<IsvPaymentTransactionModel> findCorrespondingPayPalPaymentTransaction(final String token,
            final CartModel cart)
    {
        return cart.getPaymentTransactions().stream()
                .filter(doesCartHavePayPalTransaction())
                .map(IsvPaymentTransactionModel.class::cast)
                .filter(txn -> txn.getEntries().stream()
                        .map(IsvPaymentTransactionEntryModel.class::cast)
                        .anyMatch(transactionHasAcceptedMatchingPayPalCreateSessionEntry(token)))
                .findFirst();
    }

    private Predicate<IsvPaymentTransactionEntryModel> transactionHasAcceptedMatchingPayPalCreateSessionEntry(
            final String token)
    {
        return isvEntry -> isvEntry.getType() == PaymentTransactionType.CREATE_SESSION
                && ACCEPT.equals(isvEntry.getTransactionStatus())
                && StringUtils.contains(isvEntry.getProperties().get(AP_SESSIONS_REPLY_MERCHANT_URL), token);
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }

    public void setPaypalRelativeReturnUrl(final String paypalRelativeReturnUrl)
    {
        this.paypalRelativeReturnUrl = paypalRelativeReturnUrl;
    }

    public void setPaypalRelativeCancelUrl(final String paypalRelativeCancelUrl)
    {
        this.paypalRelativeCancelUrl = paypalRelativeCancelUrl;
    }
}
