package isv.sap.payment.service.executor.request.converter.paypal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_BILLING_AGREEMENT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CANCEL_ORDER_SERVICE_ORDER_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CANCEL_ORDER_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CANCEL_ORDER;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment cancel order request.
 */
public class CancelOrderRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final String billingAgreementID = resolveStringParam(source, AP_BILLING_AGREEMENT_ID);

        final PaymentTransaction cancel = requestFactory.request(CANCEL_ORDER)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_CANCEL_ORDER_SERVICE_RUN, true);

        if (isBlank(billingAgreementID))
        {
            final PaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);
            cancel.addParam(AP_CANCEL_ORDER_SERVICE_ORDER_REQUEST_ID, entry.getRequestId());
        }
        else
        {
            cancel.addParam(AP_BILLING_AGREEMENT_ID, billingAgreementID);
        }

        return cancel.request();
    }
}

