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
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_SESSIONS_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CHECK_STATUS;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for PayPal check status request.
 */
public class CheckStatusRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final String billingAgreementId = resolveStringParam(source, AP_BILLING_AGREEMENT_ID);

        final PaymentTransaction transaction = requestFactory.request(CHECK_STATUS)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID).toString())
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_CHECK_STATUS_SERVICE_RUN, true);

        if (isNotBlank(billingAgreementId))
        {
            transaction.addParam(AP_BILLING_AGREEMENT_ID, billingAgreementId);
        }
        else
        {
            final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);
            transaction.addParam(AP_CHECK_STATUS_SERVICE_SESSIONS_REQUEST_ID, transactionEntry.getRequestId());
        }

        return transaction.request();
    }
}
