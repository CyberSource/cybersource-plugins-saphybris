package isv.sap.payment.service.executor.request.converter.fraud;

import java.math.BigDecimal;

import com.google.inject.Inject;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.service.request.RequestFactory;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.Fraud.ADVANCED_FRAUD_SCREEN;

public class AdvancedFraudScreenRequestConverter implements Converter<PaymentServiceRequest, Request>
{
    @Inject
    private RequestFactory requestFactory;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final BigDecimal amount = BigDecimal.valueOf(order.getTotalPrice());
        final AddressModel address = order.getPaymentInfo().getBillingAddress();

        return requestFactory.request(ADVANCED_FRAUD_SCREEN)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(AFS_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(DECISION_MANAGER_ENABLED, (boolean) source.getRequiredParam(DECISION_MANAGER_ENABLED))
                .addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                .addParam(BILL_TO_LAST_NAME, address.getLastname())
                .addParam(BILL_TO_EMAIL, address.getEmail())
                .addParam(BILL_TO_COUNTRY, address.getCountry().getIsocode())
                .addParam(BILL_TO_CITY, address.getTown())
                .addParam(BILL_TO_STREET1, address.getLine1())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, amount)
                .request();
    }
}
