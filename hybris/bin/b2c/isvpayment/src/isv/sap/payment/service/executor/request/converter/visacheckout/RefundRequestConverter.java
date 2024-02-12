package isv.sap.payment.service.executor.request.converter.visacheckout;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.executor.request.converter.creditcard.RefundFollowOnRequestConverter;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_SOLUTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_ORDER_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_PAYMENT_SOLUTION_VALUE;
import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.REFUND;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for visa checkout payment refund follow-on request.
 */
public class RefundRequestConverter extends RefundFollowOnRequestConverter
{
    @Override
    protected PaymentTransaction populateSpecificFields(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(REFUND)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(VC_ORDER_ID, source.getParam(VC_ORDER_ID))
                .addParam(CC_CAPTURE_SERVICE_AUTH_REQUEST_ID, entry.getRequestId())
                .addParam(CC_CAPTURE_SERVICE_RUN, TRUE)
                .addParam(PAYMENT_SOLUTION, VC_PAYMENT_SOLUTION_VALUE);
    }
}
