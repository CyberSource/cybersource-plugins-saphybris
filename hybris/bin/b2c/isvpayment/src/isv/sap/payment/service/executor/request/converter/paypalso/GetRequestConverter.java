package isv.sap.payment.service.executor.request.converter.paypalso;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_EC_GET_DETAILS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.GET;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal get request.
 */
public class GetRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel payPalSetTransactionEntry = source.getRequiredParam(SET_TRANSACTION);

        final String payPalToken = getPaypalToken(payPalSetTransactionEntry);

        return requestFactory.request(GET)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_ID,
                        payPalSetTransactionEntry.getRequestId())
                .addParam(PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN,
                        payPalSetTransactionEntry.getRequestToken())
                .addParam(PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_TOKEN, payPalToken)
                .addParam(PAYPAL_EC_GET_DETAILS_SERVICE_RUN, true)
                .request();
    }

    protected String getPaypalToken(final PaymentTransactionEntryModel entry)
    {
        final IsvPaymentTransactionEntryModel transactionEntry = (IsvPaymentTransactionEntryModel) entry;

        final String payPalEcSetReplyPaypalToken = transactionEntry.getProperties().get("payPalEcSetReplyPaypalToken");

        notNull(payPalEcSetReplyPaypalToken,
                String.format("Paypal Token is missing on transaction [%s]", transactionEntry.getCode()));

        return payPalEcSetReplyPaypalToken;
    }
}
