package isv.sap.payment.service.executor.request.converter.fraud;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.DEVICE_FINGERPRINT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DME_SERVICE_EVENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DME_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentServiceConstants.Fraud.ACCOUNT_TAKEOVER_PROTECTION;

public class AccountTakeoverProtectionRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        return requestFactory.request(ACCOUNT_TAKEOVER_PROTECTION)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, source.getRequiredParam(MERCHANT_REFERENCE_CODE))
                .addParam(DEVICE_FINGERPRINT_ID, source.getRequiredParam(DEVICE_FINGERPRINT_ID))
                .addParam(DME_SERVICE_EVENT_TYPE, source.getRequiredParam(DME_SERVICE_EVENT_TYPE))
                .addParam(DME_SERVICE_RUN, true)
                .request();
    }
}
