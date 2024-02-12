package isv.sap.payment.service.executor.request.converter.paypalso;

import java.math.BigDecimal;

import com.google.common.base.Optional;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.enums.PayPalSOCaptureTransactionType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_DO_CAPTURE_SERVICE_COMPLETE_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYPAL_DO_CAPTURE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.CAPTURE;
import static org.apache.commons.lang3.Validate.notNull;

public class CaptureRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        final Boolean isCompleteCapture =
                Optional.<Boolean>fromNullable((Boolean) source.getParam("isCompleteCapture")).or(Boolean.TRUE);

        return requestFactory.request(CAPTURE)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PAYPAL_DO_CAPTURE_SERVICE_COMPLETE_TYPE, isCompleteCapture
                        ? PayPalSOCaptureTransactionType.COMPLETE
                        : PayPalSOCaptureTransactionType.NOT_COMPLETE)
                .addParam(PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_ID, getPayPalAuthorizeTransactionId(entry))
                .addParam(PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID, entry.getRequestId())
                .addParam(PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN, entry.getRequestToken())
                .addParam(PAYPAL_DO_CAPTURE_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .request();
    }

    private String getPayPalAuthorizeTransactionId(final PaymentTransactionEntryModel entry)
    {
        final IsvPaymentTransactionEntryModel transaction = (IsvPaymentTransactionEntryModel) entry;
        final String authorizeID = transaction.getProperties().get("payPalAuthorizationReplyTransactionId");

        notNull(authorizeID,
                String.format("The authorization is missing on transaction entry [%s]", transaction.getCode()));

        return authorizeID;
    }
}
