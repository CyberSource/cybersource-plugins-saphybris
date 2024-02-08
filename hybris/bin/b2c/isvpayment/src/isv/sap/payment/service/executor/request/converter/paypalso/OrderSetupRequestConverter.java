package isv.sap.payment.service.executor.request.converter.paypalso;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.GET_TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.ORDER_SETUP;
import static org.apache.commons.lang3.Validate.notNull;

public class OrderSetupRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final BigDecimal totalPrice = BigDecimal.valueOf(order.getTotalPrice());
        final PaymentInfoModel paymentInfo = order.getPaymentInfo();

        notNull(paymentInfo, "paymentInfo is required on cart");

        final PaymentTransactionEntryModel set = source.getRequiredParam(SET_TRANSACTION);
        final PaymentTransactionEntryModel get = source.getRequiredParam(GET_TRANSACTION);

        return requestFactory.request(ORDER_SETUP)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, totalPrice)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_RUN, true)
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_TOKEN,
                        propertyValue(PAYPAL_EC_SET_REPLY_PAYPAL_TOKEN, set))
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_PAYER_ID,
                        propertyValue(PAYPAL_EC_GET_DETAILS_REPLY_PAYER_ID, get))
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_CUSTOMER_EMAIL,
                        paymentInfo.getBillingAddress().getEmail())
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_ID, set.getRequestId())
                .addParam(PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, set.getRequestToken())
                .addParam(DEVICE_FINGERPRINT_ID, order.getFingerPrintSessionID())
                .request();
    }

    private String propertyValue(final String key, final PaymentTransactionEntryModel entry)
    {
        final IsvPaymentTransactionEntryModel isvEntry = (IsvPaymentTransactionEntryModel) entry;
        final String propertyValue = isvEntry.getProperties().get(key);

        notNull(propertyValue, String.format("paypal [%s] not found on transaction [%s].", key, entry.getCode()));

        return propertyValue;
    }
}
