package isv.sap.payment.service.executor.request.converter.alternative;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import org.apache.commons.lang.StringUtils;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_CHECK_STATUS_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_INITIATE_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_RECONCILIATION_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_CHECK_STATUS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.CHECK_STATUS;

public class CheckStatusRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);
        final AlternativePaymentMethod apMethod = source.getRequiredParam("alternatePaymentMethod");
        final String check = isInitiateTransactionCheck(source) ? AP_CHECK_STATUS_SERVICE_INITIATE_REQUEST_ID
                : AP_CHECK_STATUS_SERVICE_CHECK_STATUS_REQUEST_ID;
        final String reconciliationID = source.getParam(AP_CHECK_STATUS_SERVICE_RECONCILIATION_ID);

        final PaymentTransaction paymentTransaction = requestFactory.request(CHECK_STATUS)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AP_CHECK_STATUS_SERVICE_RUN, true)
                .addParam(AP_PAYMENT_TYPE, apMethod.getCode())
                .addParam(check, entry.getRequestId());

        if (!StringUtils.isEmpty(reconciliationID))
        {
            paymentTransaction.addParam(AP_CHECK_STATUS_SERVICE_RECONCILIATION_ID, reconciliationID);
        }

        return paymentTransaction.request();
    }

    private boolean isInitiateTransactionCheck(final PaymentServiceRequest source)
    {
        return PaymentTransactionType.INITIATE.equals(source.getRequiredParam("paymentTransactionType"));
    }
}
