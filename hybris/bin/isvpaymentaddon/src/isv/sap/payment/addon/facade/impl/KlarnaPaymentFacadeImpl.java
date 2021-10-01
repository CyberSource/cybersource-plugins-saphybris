package isv.sap.payment.addon.facade.impl;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.alternative.CreateSessionRequestBuilder;
import isv.cjl.payment.service.executor.request.builder.alternative.UpdateSessionRequestBuilder;
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;
import isv.sap.payment.addon.facade.KlarnaPaymentFacade;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.BILL_TO_LANGUAGE;
import static isv.cjl.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL;
import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL;
import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL;
import static isv.sap.payment.enums.AlternativePaymentMethod.KLI;

public class KlarnaPaymentFacadeImpl extends AbstractPaymentFacade implements KlarnaPaymentFacade
{
    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Override
    public String createKlarnaSession(final CartModel sessionCart)
    {
        final PaymentServiceResult result = executeRequest(
                new CreateSessionRequestBuilder()
                        .setMerchantId(getMerchantService().getCurrentMerchant(ALTERNATIVE_PAYMENT).getId())
                        .setSuccessURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_RETURN_URL), true)
                                        + KLI.getCode())
                        .setFailureURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_FAILED_URL), true))
                        .setCancelURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_CANCEL_URL), true))
                        .addParam(ORDER, sessionCart)
                        .addParam(BILL_TO_LANGUAGE, getSiteConfigService()
                                .getProperty(IsvPaymentAddonConstants.AlternativePayments.KLARNA_LANGUAGE))
                        .build());

        final IsvPaymentTransactionEntryModel txnEntry = result.getData(TRANSACTION);

        if (!ACCEPT.equals(txnEntry.getTransactionStatus()))
        {
            throw new IllegalStateException(
                    "Klarna create session call wasn't successful, decision was: " + txnEntry.getTransactionStatus());
        }

        return txnEntry.getProperties().get("apSessionsReplyProcessorToken");
    }

    @Override
    public String updateKlarnaSession(final CartModel sessionCart)
    {
        final PaymentTransactionEntryModel transactionEntry = paymentTransactionService
                .getLatestTransaction(PaymentType.ALTERNATIVE_PAYMENT, sessionCart)
                .map(transaction -> paymentTransactionService
                        .getLatestAcceptedTransactionEntry(transaction, PaymentTransactionType.CREATE_SESSION))
                .get()
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to find Klarna create session transaction for order: " + sessionCart.getGuid()));

        final PaymentServiceResult result = executeRequest(
                new UpdateSessionRequestBuilder()
                        .setMerchantId(getMerchantService().getCurrentMerchant(ALTERNATIVE_PAYMENT).getId())
                        .setBillToLanguage(getSiteConfigService()
                                .getProperty(IsvPaymentAddonConstants.AlternativePayments.KLARNA_LANGUAGE))
                        .setSuccessURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_RETURN_URL), true)
                                        + KLI.getCode())
                        .setFailureURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_FAILED_URL), true))
                        .setCancelURL(
                                convertToAbsoluteURL(getSiteConfigService().getProperty(RELATIVE_CANCEL_URL), true))
                        .addParam(ORDER, sessionCart)
                        .addParam(TRANSACTION, transactionEntry)
                        .build());

        final IsvPaymentTransactionEntryModel txnEntry = result.getData(TRANSACTION);

        if (!ACCEPT.equals(txnEntry.getTransactionStatus()))
        {
            throw new IllegalStateException(
                    "Klarna update session call wasn't successful, decision was: " + txnEntry.getTransactionStatus());
        }

        return txnEntry.getProperties().get("apSessionsReplyProcessorToken");
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }
}
