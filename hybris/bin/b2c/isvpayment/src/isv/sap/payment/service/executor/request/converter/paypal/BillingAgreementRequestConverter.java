package isv.sap.payment.service.executor.request.converter.paypal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_BILLING_AGREEMENT_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_BILLING_AGREEMENT_SERVICE_SESSIONS_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.BILLING_AGREEMENT;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment billing agreement request.
 */
public class BillingAgreementRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(BILLING_AGREEMENT)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_BILLING_AGREEMENT_SERVICE_RUN, true)
                .addParam(AP_BILLING_AGREEMENT_SERVICE_SESSIONS_REQUEST_ID, transactionEntry.getRequestId())
                .request();
    }
}
