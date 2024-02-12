package isv.sap.payment.addon.facade.impl;

import java.util.Map;

import de.hybris.platform.core.model.order.CartModel;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.builder.googlepay.AuthorizationRequestBuilder;
import isv.sap.payment.addon.facade.GooglePayPaymentFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.REVIEW;
import static isv.cjl.payment.enums.PaymentType.GOOGLE_PAY;

public class GooglePayPaymentFacadeImpl extends AbstractPaymentFacade implements GooglePayPaymentFacade
{
    @Override
    public boolean authorizeGooglePayPayment(final Map<String, Object> paymentData, final CartModel cart)
    {
        final PaymentServiceResult authorizationResult = executeRequest(
                new AuthorizationRequestBuilder()
                        .setMerchantId(getMerchantService().getCurrentMerchant(GOOGLE_PAY).getId())
                        .setPaymentData(paymentData)
                        .addParam(ORDER, cart)
                        .build());

        final IsvPaymentTransactionEntryModel transaction = authorizationResult.getData(TRANSACTION);

        return isTransactionInState(transaction, ACCEPT, REVIEW);
    }
}
