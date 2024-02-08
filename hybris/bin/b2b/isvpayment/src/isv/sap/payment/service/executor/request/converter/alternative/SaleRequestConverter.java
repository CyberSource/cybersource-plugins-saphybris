package isv.sap.payment.service.executor.request.converter.alternative;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.SALE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SaleRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest request)
    {
        final AbstractOrderModel cart = request.getRequiredParam(ORDER);
        final String optionId = request.getParam(AP_SALE_SERVICE_PAYMENT_OPTION_ID);
        final String descriptor = request.getParam(INVOICE_HEADER_MERCHANT_DESCRIPTOR);
        final Integer transactionTimeout = request.getParam(AP_SALE_SERVICE_TRANSACTION_TIMEOUT);

        final PaymentTransaction sale = requestFactory.request(SALE)
                .addParam(MERCHANT_ID, request.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(AP_PAYMENT_TYPE, request.getRequiredParam(AP_PAYMENT_TYPE))
                .addParam(AP_SALE_SERVICE_CANCEL_URL, request.getParam(AP_SALE_SERVICE_CANCEL_URL))
                .addParam(AP_SALE_SERVICE_SUCCESS_URL, request.getParam(AP_SALE_SERVICE_SUCCESS_URL))
                .addParam(AP_SALE_SERVICE_FAILURE_URL, request.getParam(AP_SALE_SERVICE_FAILURE_URL))
                .addParam(AP_SALE_SERVICE_RUN, true)
                .addParam(INVOICE_HEADER_MERCHANT_DESCRIPTOR, descriptor)
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(cart.getTotalPrice()))
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode());

        if (isNotBlank(optionId))
        {
            sale.addParam(AP_SALE_SERVICE_PAYMENT_OPTION_ID, optionId);
        }

        if (cart.getPaymentInfo() != null)
        {
            setBillingAddress(sale, cart.getPaymentInfo().getBillingAddress());
        }

        if (transactionTimeout != null)
        {
            sale.addParam(AP_SALE_SERVICE_TRANSACTION_TIMEOUT, transactionTimeout);
        }

        return sale.request();
    }

    protected void setBillingAddress(final PaymentTransaction sale, final AddressModel address)
    {
        if (address != null)
        {
            sale.addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                    .addParam(BILL_TO_LAST_NAME, address.getLastname())
                    .addParam(BILL_TO_COMPANY, address.getCompany())
                    .addParam(BILL_TO_EMAIL, address.getEmail())
                    .addParam(BILL_TO_CITY, address.getTown())
                    .addParam(BILL_TO_STREET1, address.getLine1());

            if (address.getCountry() != null)
            {
                sale.addParam(BILL_TO_COUNTRY, address.getCountry().getIsocode());
            }

            if (address.getRegion() != null)
            {
                sale.addParam(BILL_TO_STATE, address.getRegion().getIsocodeShort());
            }
        }
    }
}
