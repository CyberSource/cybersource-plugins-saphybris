package isv.sap.payment.service.executor.request.converter.alternative;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_INITIATE_SERVICE_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_INITIATE_SERVICE_RECONCILIATION_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_INITIATE_SERVICE_RETURN_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_INITIATE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.INITIATE;
import static java.math.BigDecimal.valueOf;

public class InitiateRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        return requestFactory.request(INITIATE)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(AP_INITIATE_SERVICE_RETURN_URL, source.getRequiredParam("returnUrl"))
                .addParam(AP_INITIATE_SERVICE_RUN, true)
                .addParam(AP_INITIATE_SERVICE_PRODUCT_NAME, source.getRequiredParam("productName"))
                .addParam(AP_PAYMENT_TYPE, source.getRequiredParam(AP_PAYMENT_TYPE))
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(cart.getTotalPrice()))
                .addParam(AP_INITIATE_SERVICE_RECONCILIATION_ID, source.getParam("reconciliationID"))
                .request();
    }
}
