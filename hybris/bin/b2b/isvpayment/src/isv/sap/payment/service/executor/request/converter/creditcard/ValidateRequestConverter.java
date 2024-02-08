package isv.sap.payment.service.executor.request.converter.creditcard;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYER_AUTH_VALIDATE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYER_AUTH_VALIDATE_SERVICE_SIGNED_PARES;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.VALIDATE;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment validate request.
 */
public class ValidateRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest request)
    {
        return populateCommonFields(populateSpecificFields(request), request).request();
    }

    protected PaymentTransaction populateSpecificFields(final PaymentServiceRequest source)
    {
        return requestFactory.request(VALIDATE)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID));
    }

    protected PaymentTransaction populateCommonFields(final PaymentTransaction transaction,
            final PaymentServiceRequest request)
    {
        final AbstractOrderModel order = request.getRequiredParam(ORDER);
        final String paRes = request.getRequiredParam("paRes");

        return transaction
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PAYER_AUTH_VALIDATE_SERVICE_RUN, TRUE)
                .addParam(PAYER_AUTH_VALIDATE_SERVICE_SIGNED_PARES, paRes);
    }
}
