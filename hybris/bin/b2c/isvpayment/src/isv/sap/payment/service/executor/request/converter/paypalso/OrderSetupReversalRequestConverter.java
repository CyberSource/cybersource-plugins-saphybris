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
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_AUTH_REVERSAL_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.ORDER_SETUP_REVERSAL;
import static org.apache.commons.lang3.Validate.notNull;

public class OrderSetupReversalRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(ORDER_SETUP_REVERSAL)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_ID,
                        getPayPalOrderSetUpReplyTransactionId(entry))
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_RUN, true)
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID, entry.getRequestId())
                .addParam(PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN, entry.getRequestToken())
                .request();
    }

    private String getPayPalOrderSetUpReplyTransactionId(final IsvPaymentTransactionEntryModel transactionEntry)
    {
        final String authorizeID = transactionEntry.getProperties().get("payPalEcOrderSetupReplyTransactionId");

        notNull(authorizeID,
                String.format("The payPalEcOrderSetupReplyTransactionId is missing on transaction entry [%s]",
                        transactionEntry.getCode()));

        return authorizeID;
    }
}
