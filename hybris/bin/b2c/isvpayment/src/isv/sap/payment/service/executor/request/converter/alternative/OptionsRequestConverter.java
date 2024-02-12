package isv.sap.payment.service.executor.request.converter.alternative;

import java.util.UUID;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.constants.PaymentRequestParamConstants;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_OPTIONS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.OPTIONS;
import static isv.cjl.payment.enums.AlternativePaymentMethod.IDL;

public class OptionsRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final Object order = source.getRequestParams().get(ORDER);
        final String mrc = (order == null) ? UUID.randomUUID().toString() : ((AbstractOrderModel) order).getGuid();

        return requestFactory.request(OPTIONS)
                .addParam(PaymentRequestParamConstants.MERCHANT_ID,
                        source.getRequiredParam(PaymentRequestParamConstants.MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, mrc)
                .addParam(AP_OPTIONS_SERVICE_RUN, true)
                .addParam(AP_PAYMENT_TYPE, IDL.getCode())
                .request();
    }
}
