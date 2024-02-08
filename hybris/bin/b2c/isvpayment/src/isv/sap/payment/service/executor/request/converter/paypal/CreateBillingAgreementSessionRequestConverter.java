package isv.sap.payment.service.executor.request.converter.paypal;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_BILLING_AGREEMENT_INDICATOR;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_CANCEL_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_SUCCESS_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CREATE_BILLING_AGREEMENT_SESSION;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for PayPal create billing agreement session request.
 */
public class CreateBillingAgreementSessionRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        return requestFactory.request(CREATE_BILLING_AGREEMENT_SESSION)
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_SESSIONS_SERVICE_RUN, true)
                .addParam(AP_SESSIONS_SERVICE_CANCEL_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_CANCEL_URL))
                .addParam(AP_SESSIONS_SERVICE_SUCCESS_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_SUCCESS_URL))
                .addParam(AP_BILLING_AGREEMENT_INDICATOR, true)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid()).request();
    }
}
