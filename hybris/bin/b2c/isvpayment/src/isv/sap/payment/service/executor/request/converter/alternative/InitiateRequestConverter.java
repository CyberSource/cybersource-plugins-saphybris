package isv.sap.payment.service.executor.request.converter.alternative;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import isv.cjl.payment.constants.PaymentRequestParamConstants;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.INITIATE;
import static java.math.BigDecimal.valueOf;

public class InitiateRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final PaymentTransaction initiate =  requestFactory.request(INITIATE)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(AP_INITIATE_SERVICE_RETURN_URL, source.getRequiredParam("returnUrl"))
                .addParam(AP_INITIATE_SERVICE_RUN, true)
                .addParam(AP_INITIATE_SERVICE_PRODUCT_NAME, source.getRequiredParam("productName"))
                .addParam(AP_PAYMENT_TYPE, source.getRequiredParam(AP_PAYMENT_TYPE))
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(cart.getTotalPrice()))
                .addParam(AP_INITIATE_SERVICE_RECONCILIATION_ID, source.getParam("reconciliationID"))
                .addParam(DEVICE_FINGERPRINT_ID, cart.getFingerPrintSessionID())
                .addParam(PaymentRequestParamConstants.DECISION_MANAGER_ENABLED,true);
        if (cart.getPaymentInfo() != null && cart.getPaymentInfo().getBillingAddress() != null)
        {
            initiate.addParam(BILL_TO_FIRST_NAME, cart.getPaymentInfo().getBillingAddress().getFirstname())
                    .addParam(BILL_TO_LAST_NAME, cart.getPaymentInfo().getBillingAddress().getLastname())
                    .addParam(BILL_TO_COMPANY, cart.getPaymentInfo().getBillingAddress().getCompany())
                    .addParam(BILL_TO_EMAIL, cart.getPaymentInfo().getBillingAddress().getEmail())
                    .addParam(BILL_TO_CITY, cart.getPaymentInfo().getBillingAddress().getTown())
                    .addParam(BILL_TO_STREET1, cart.getPaymentInfo().getBillingAddress().getLine1())
                    .addParam(BILL_TO_POSTAL_CODE,cart.getPaymentInfo().getBillingAddress().getPostalcode());

            if (cart.getPaymentInfo().getBillingAddress().getCountry() != null)
            {
                initiate.addParam(BILL_TO_COUNTRY, cart.getPaymentInfo().getBillingAddress().getCountry().getIsocode());
            }

            if (cart.getPaymentInfo().getBillingAddress().getRegion() != null)
            {
                initiate.addParam(BILL_TO_STATE, cart.getPaymentInfo().getBillingAddress().getRegion().getIsocodeShort());
            }
        }

        return initiate.request();
    }
}
