package isv.sap.payment.service.executor.request.converter.visacheckout;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PAYMENT_SOLUTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_GET_DATA_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_ORDER_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.VC_PAYMENT_SOLUTION_VALUE;
import static isv.cjl.payment.constants.PaymentServiceConstants.VisaCheckout.GET;
import static java.lang.Boolean.TRUE;

public class GetRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);

        return requestFactory.request(GET)
                .addParam(VC_GET_DATA_SERVICE_RUN, TRUE)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(VC_ORDER_ID, source.getParam(VC_ORDER_ID))
                .addParam(PAYMENT_SOLUTION, VC_PAYMENT_SOLUTION_VALUE)
                .request();
    }
}
