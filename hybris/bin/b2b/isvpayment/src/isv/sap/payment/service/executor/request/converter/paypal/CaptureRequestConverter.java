package isv.sap.payment.service.executor.request.converter.paypal;

import com.google.common.base.Optional;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CAPTURE_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CAPTURE_SERVICE_IS_FINAL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CAPTURE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CAPTURE;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static java.math.BigDecimal.valueOf;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment capture request.
 */
public class CaptureRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);

        final Boolean isCaptureFinal = Optional.fromNullable((Boolean) source.getParam("isCaptureFinal"))
                .or(Boolean.TRUE);

        return requestFactory.request(CAPTURE)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_CAPTURE_SERVICE_RUN, true)
                .addParam(AP_CAPTURE_SERVICE_AUTH_REQUEST_ID, transactionEntry.getRequestId())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(order.getTotalPrice()))
                .addParam(AP_CAPTURE_SERVICE_IS_FINAL, isCaptureFinal)
                .request();
    }
}
