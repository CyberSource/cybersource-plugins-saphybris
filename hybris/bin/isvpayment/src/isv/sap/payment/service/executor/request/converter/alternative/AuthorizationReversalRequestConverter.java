package isv.sap.payment.service.executor.request.converter.alternative;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_AUTH_REVERSAL_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.AUTHORIZATION_REVERSAL;
import static isv.cjl.payment.enums.AlternativePaymentMethod.KLI;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Klarna authorization reversal request.
 */
public class AuthorizationReversalRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel transaction = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(AUTHORIZATION_REVERSAL)
                .addParam(AP_PAYMENT_TYPE, KLI.getCode())
                .addParam(AP_AUTH_REVERSAL_SERVICE_RUN, true)
                .addParam(AP_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, transaction.getRequestId())
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .request();
    }
}
