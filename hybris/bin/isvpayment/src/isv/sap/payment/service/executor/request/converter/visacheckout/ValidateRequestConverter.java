package isv.sap.payment.service.executor.request.converter.visacheckout;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PARES;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYER_AUTH_VALIDATE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYER_AUTH_VALIDATE_SERVICE_SIGNED_PARES;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_SOLUTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_ORDER_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_PAYMENT_SOLUTION_VALUE;
import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.VALIDATE;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Visa Checkout validate request.
 */
public class ValidateRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);

        return requestFactory.request(VALIDATE)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(VC_ORDER_ID, source.getParam(VC_ORDER_ID))
                .addParam(PAYER_AUTH_VALIDATE_SERVICE_SIGNED_PARES, source.getParam(PARES))
                .addParam(PAYER_AUTH_VALIDATE_SERVICE_RUN, TRUE)
                .addParam(PAYMENT_SOLUTION, VC_PAYMENT_SOLUTION_VALUE).request();
    }
}
