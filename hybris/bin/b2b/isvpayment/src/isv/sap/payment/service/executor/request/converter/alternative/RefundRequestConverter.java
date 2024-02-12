package isv.sap.payment.service.executor.request.converter.alternative;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_REFUND_SERVICE_AP_INITIATE_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_REFUND_SERVICE_REASON;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_REFUND_SERVICE_REFUND_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_REFUND_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.REFUND;
import static isv.cjl.payment.enums.AlternativePaymentMethod.APY;
import static isv.cjl.payment.enums.AlternativePaymentMethod.AYM;
import static org.apache.commons.lang.StringUtils.EMPTY;

public class RefundRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        final AlternativePaymentMethod apMethod = source.getRequiredParam("alternatePaymentMethod");
        final String reason = apMethod == AYM ? source.getParam("reason") : EMPTY;

        return requestFactory.request(REFUND)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, apMethod.getCode())
                .addParam(AP_REFUND_SERVICE_RUN, true)
                .addParam(AP_REFUND_SERVICE_REFUND_REQUEST_ID, entry.getRequestId())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, source.getRequiredParam("amount"))
                .addParam(AP_REFUND_SERVICE_REASON, reason)
                .addParam(AP_REFUND_SERVICE_AP_INITIATE_REQUEST_ID, isAliPay(apMethod) ? entry.getRequestId() : EMPTY)
                .request();
    }

    private boolean isAliPay(final AlternativePaymentMethod method)
    {
        return method == APY || method == AYM;
    }
}
