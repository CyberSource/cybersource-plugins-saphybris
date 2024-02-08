package isv.sap.payment.service.executor.request.converter.paypal;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.PAYER_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_ORDER_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_ORDER_SERVICE_SESSIONS_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYER_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.ORDER_SETUP;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment order setup request.
 */
public class OrderSetupRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);
        final String payerID = valueOf(defaultIfNull(source.getParam(PAYER_ID), EMPTY));
        final BigDecimal totalPrice = BigDecimal.valueOf(order.getTotalPrice());

        return requestFactory.request(ORDER_SETUP)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_ORDER_SERVICE_RUN, true)
                .addParam(AP_ORDER_SERVICE_SESSIONS_REQUEST_ID, transactionEntry.getRequestId())
                .addParam(AP_PAYER_ID, payerID)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, totalPrice)
                .request();
    }
}
