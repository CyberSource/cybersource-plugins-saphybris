package isv.sap.payment.service.executor.request.converter.reporting;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for reporting transaction request.
 */
public class ReportingTransactionRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final String merchantId = source.getRequiredParam(MERCHANT_ID);

        final AbstractOrderModel order = source.getRequiredParam(ORDER);

        return requestFactory.request()
                .addParam(MERCHANT_ID, merchantId)
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .request();
    }
}
