package isv.sap.payment.service.executor.request.converter.alternative;

import java.math.BigDecimal;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.constants.PaymentRequestParamConstants;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentServiceConstants.Klarna.AUTHORIZATION;
import static isv.cjl.payment.enums.AlternativePaymentMethod.KLI;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Klarna authorization request.
 */
public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);
        final AddressModel billingAddress = cart.getPaymentInfo().getBillingAddress();
        String billingIsoCode = billingAddress.getRegion().getIsocode();
        String[] stateIsoCode = billingIsoCode.split("-",2);
        return requestFactory.request(AUTHORIZATION)
                .addParam(PaymentRequestParamConstants.AP_PAYMENT_TYPE, KLI.getCode())
                .addParam(PaymentRequestParamConstants.AP_AUTH_SERVICE_RUN, true)
                .addParam(PaymentRequestParamConstants.AP_AUTH_SERVICE_PREAPPROVAL_TOKEN,
                        source.getRequiredParam("apAuthServicePreapprovalToken"))
                .addParam(PaymentRequestParamConstants.MERCHANT_ID,
                        source.getRequiredParam(PaymentRequestParamConstants.MERCHANT_ID))
                .addParam(PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE, cart.getGuid())

                .addParam(PaymentRequestParamConstants.BILL_TO_CITY, billingAddress.getTown())
                .addParam(PaymentRequestParamConstants.BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(PaymentRequestParamConstants.BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(PaymentRequestParamConstants.BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(PaymentRequestParamConstants.BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(PaymentRequestParamConstants.BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(PaymentRequestParamConstants.BILL_TO_STATE,stateIsoCode[1])
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT,
                        BigDecimal.valueOf(cart.getTotalPrice()))
                // optionals
                .addParam(PaymentRequestParamConstants.BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())

                .request();
    }
}
