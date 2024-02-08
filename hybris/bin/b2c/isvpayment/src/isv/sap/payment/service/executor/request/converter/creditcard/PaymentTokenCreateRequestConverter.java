package isv.sap.payment.service.executor.request.converter.creditcard;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAY_SUBSCRIPTION_CREATE_SERVICE_PAYMENT_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.RECURRING_SUBSCRIPTION_INFO_FREQUENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.SUBSCRIPTION_CREATE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.CREATE_TOKEN;
import static isv.cjl.payment.enums.RecurringFrequency.ON_DEMAND;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment token create request.
 */
public class PaymentTokenCreateRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel entry = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(CREATE_TOKEN)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(SUBSCRIPTION_CREATE_SERVICE_RUN, true)
                .addParam(PAY_SUBSCRIPTION_CREATE_SERVICE_PAYMENT_REQUEST_ID, entry.getRequestId())
                .addParam(RECURRING_SUBSCRIPTION_INFO_FREQUENCY, ON_DEMAND)
                .request();
    }
}
