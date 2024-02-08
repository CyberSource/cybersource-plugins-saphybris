package isv.sap.payment.service.executor.request.converter.visacheckout;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_SOLUTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_ORDER_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_PAYMENT_SOLUTION_VALUE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VOID_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VOID_SERVICE_VOID_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.VOID;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link isv.cjl.payment.service.request.Request} for visa checkout payment void request.
 */
public class VoidRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(VOID)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(VC_ORDER_ID, source.getParam(VC_ORDER_ID))
                .addParam(VOID_SERVICE_VOID_REQUEST_ID, entry.getRequestId())
                .addParam(VOID_SERVICE_RUN, TRUE)
                .addParam(PAYMENT_SOLUTION, VC_PAYMENT_SOLUTION_VALUE)
                .request();
    }
}
