package isv.sap.payment.service.executor.request.converter.visacheckout;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.AUTHORIZATION_REVERSAL;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link isv.cjl.payment.service.request.Request} for visa checkout payment capture request.
 */
public class AuthorizationReversalRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(AUTHORIZATION_REVERSAL)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(VC_ORDER_ID, source.getParam(VC_ORDER_ID))
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(CC_AUTH_SERVICE_RUN, TRUE)
                .addParam(CC_AUTH_REVERSAL_SERVICE_RUN, TRUE)
                .addParam(CC_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, entry.getRequestId())
                .addParam(PAYMENT_SOLUTION, VC_PAYMENT_SOLUTION_VALUE)
                .request();
    }
}
