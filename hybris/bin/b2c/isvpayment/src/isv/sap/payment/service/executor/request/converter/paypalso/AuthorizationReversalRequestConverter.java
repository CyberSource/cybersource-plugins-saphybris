package isv.sap.payment.service.executor.request.converter.paypalso;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.AUTHORIZATION_REVERSAL;
import static org.apache.commons.lang3.Validate.notNull;

public class AuthorizationReversalRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(AUTHORIZATION_REVERSAL)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_ID,
                        getPayPalAuthorizeReplyTransactionId(entry))
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_RUN, true)
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID, entry.getRequestId())
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN, entry.getRequestToken())
                .request();
    }

    private String getPayPalAuthorizeReplyTransactionId(final IsvPaymentTransactionEntryModel transactionEntry)
    {
        final String authorizeID = transactionEntry.getProperties().get("payPalAuthorizationReplyTransactionId");

        notNull(authorizeID,
                String.format("The payPalAuthorizationReplyTransactionId is missing on transaction entry [%s]",
                        transactionEntry.getCode()));

        return authorizeID;
    }
}
