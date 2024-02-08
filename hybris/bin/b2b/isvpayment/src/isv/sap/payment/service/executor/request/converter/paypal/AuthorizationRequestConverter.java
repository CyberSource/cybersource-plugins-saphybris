package isv.sap.payment.service.executor.request.converter.paypal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import isv.cjl.payment.constants.PaymentRequestParamConstants;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_AUTH_SERVICE_ORDER_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_AUTH_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.AUTHORIZATION;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static java.math.BigDecimal.valueOf;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment Authorization request.
 */
public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(AUTHORIZATION)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID).toString())
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_AUTH_SERVICE_RUN, true)
                .addParam(AP_AUTH_SERVICE_ORDER_REQUEST_ID, transactionEntry.getRequestId())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(order.getTotalPrice()))
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(DEVICE_FINGERPRINT_ID, order.getFingerPrintSessionID())
                .addParam(PaymentRequestParamConstants.DECISION_MANAGER_ENABLED,true)
                .request();
    }
}
