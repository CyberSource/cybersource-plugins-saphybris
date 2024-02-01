package isv.sap.payment.service.executor.request.converter.paypalso;

import java.math.BigDecimal;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.GET_TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER_SETUP_TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.AUTHORIZATION;
import static isv.cjl.payment.utils.PaymentParamUtils.getValue;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment Authorization request.
 */
public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest request)
    {
        final AbstractOrderModel order = request.getRequiredParam(ORDER);

        final PaymentTransaction payPalAuthorization = setRequredFields(order, request);

        setBillingFields(order, payPalAuthorization);

        return payPalAuthorization.request();
    }

    protected void setBillingFields(final AbstractOrderModel order,
            final PaymentTransaction payPalAuthorization)
    {
        final Optional<AddressModel> billingAddress = Optional.fromNullable(order.getPaymentInfo().getBillingAddress());

        if (billingAddress.isPresent())
        {
            final AddressModel address = billingAddress.get();

            payPalAuthorization.addParam(BILL_TO_CITY, address.getTown())
                    .addParam(BILL_TO_COMPANY, address.getCompany())
                    .addParam(BILL_TO_COUNTRY, address.getCountry().getIsocode())
                    .addParam(BILL_TO_COUNTY, address.getDistrict())
                    .addParam(BILL_TO_EMAIL, address.getEmail())
                    .addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                    .addParam(BILL_TO_LAST_NAME, address.getLastname())
                    .addParam(BILL_TO_PHONE_NUMBER, address.getPhone1())
                    .addParam(BILL_TO_POSTAL_CODE, address.getPostalcode())
                    .addParam(BILL_TO_STATE, getValue(address.getRegion(), new Function<RegionModel, String>()
                    {
                        @Override
                        public String apply(final RegionModel input)
                        {
                            return input.getIsocodeShort();
                        }
                    }))
                    .addParam(BILL_TO_STREET1, address.getLine1())
                    .addParam(BILL_TO_STREET2, address.getLine2())
                    .addParam(BILL_TO_STREET3, address.getBuilding());
        }
    }

    protected PaymentTransaction setRequredFields(final AbstractOrderModel order,
            final PaymentServiceRequest request)
    {
        final IsvPaymentTransactionEntryModel orderGetTransactionEntry = request.getRequiredParam(GET_TRANSACTION);
        final IsvPaymentTransactionEntryModel orderSetupTransactionEntry = request
                .getRequiredParam(ORDER_SETUP_TRANSACTION);

        return requestFactory.request(AUTHORIZATION)
                .addParam(MERCHANT_ID, request.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PAYPAL_AUTHORIZATION_SERVICE_RUN, true)

                .addParam(PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_CUSTOMER_EMAIL,
                        orderGetTransactionEntry.getProperties().get(PAYPAL_EC_GET_DETAILS_REPLY_PAYER))
                .addParam(PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID,
                        orderSetupTransactionEntry.getRequestId())
                .addParam(PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN,
                        orderSetupTransactionEntry.getRequestToken())

                .addParam(PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_ORDER_ID,
                        orderSetupTransactionEntry.getProperties().get(PAYPAL_EC_ORDER_SETUP_REPLY_TRANSACTION_ID))

                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(DEVICE_FINGERPRINT_ID, order.getFingerPrintSessionID());
    }
}
