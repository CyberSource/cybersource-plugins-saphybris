package isv.sap.payment.service.executor.request.converter.alternative;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CAPTURE_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CAPTURE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.CAPTURE;
import static isv.cjl.payment.enums.AlternativePaymentMethod.KLI;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Klarna capture request.
 */
public class CaptureRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(PaymentConstants.CommonFields.ORDER);
        final IsvPaymentTransactionEntryModel entry = source
                .getRequiredParam(PaymentConstants.CommonFields.TRANSACTION);

        return requestFactory.request(CAPTURE)
                .addParam(AP_PAYMENT_TYPE, KLI.getCode())
                .addParam(AP_CAPTURE_SERVICE_RUN, true)
                .addParam(AP_CAPTURE_SERVICE_AUTH_REQUEST_ID, entry.getRequestId())
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .request();
    }
}
