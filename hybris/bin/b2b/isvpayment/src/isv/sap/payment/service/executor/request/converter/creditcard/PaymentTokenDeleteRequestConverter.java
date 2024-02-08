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
import static isv.cjl.payment.constants.PaymentRequestParamConstants.RECURRING_SUBSCRIPTION_INFO_SUBSCRIPTION_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.SUBSCRIPTION_DELETE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.DELETE_TOKEN;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment token delete request.
 */
public class PaymentTokenDeleteRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel createSubscription = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(DELETE_TOKEN)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(RECURRING_SUBSCRIPTION_INFO_SUBSCRIPTION_ID, createSubscription.getSubscriptionID())
                .addParam(SUBSCRIPTION_DELETE_SERVICE_RUN, true)
                .request();
    }
}
